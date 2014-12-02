package bitsetDemo;

import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

public class PooledByteBufferAllocator {

	private abstract class AllocatorIndexStrategy {
		protected int numBuffers = 0;
		protected int numAvailable = 0;
		protected Condition availCond;

		public void init(int numBuffers) {
			if (numBuffers < 0) {
				throw new RuntimeException("numBuffers must be >= 0");
			}
			this.numBuffers = this.numAvailable = numBuffers;
			this.availCond = null;
		}

		public int getNumAvailable() {
			return this.numAvailable;
		}

		public int getNumTotal() {
			return this.numBuffers;
		}

		public int nextFreeIndex(Condition signalWhenAvail) {
			int bufId = nextFreeIndex();
			if (bufId == -1) {
				availCond = signalWhenAvail;
			}
			return bufId;
		}

		public abstract void markUsed(int bufIndex);

		// Implementors need to signal the availCond
		// if it is non-null and the buffer was marked as free
		public abstract void markFree(int bufIndex);

		public abstract int nextFreeIndex();

		public abstract void clear();
	}

	private static final int PRIMITIVE_MAX_BUFS = Long.SIZE;

	private class PrimitiveAllocatorIndexStrategy extends
			AllocatorIndexStrategy {

		private long usedBits = 0;

		private void mark(int index, boolean set) {
			if (set)
				usedBits |= (1l << index);
			else
				usedBits ^= (1l << index);
		}

		@Override
		public void init(int numBuffers) {
			if (numBuffers > PRIMITIVE_MAX_BUFS) {
				throw new RuntimeException("Unable to allocate > 64 buffers");
			}
			super.init(numBuffers);
			usedBits = 0;
		}

		@Override
		public int nextFreeIndex() {
			for (int bit = 0; bit < numBuffers; ++bit) {
				if (((usedBits >>> bit) & 0x1) == 0) {
					return bit;
				}
			}
			return -1;
		}

		@Override
		public void clear() {
			usedBits = 0;
			numAvailable = numBuffers;
		}

		@Override
		public void markFree(int bufIndex) {
			if (bufIndex < 0 || bufIndex >= numBuffers) {
				throw new RuntimeException("bufIndex must be >= 0 and < "
						+ numBuffers);
			}

			if (((usedBits >>> bufIndex) & 0x1) == 1) {
				++numAvailable;
			}
			mark(bufIndex, false);

			if (availCond != null) {
				availCond.signal();
				availCond = null;
			}
		}

		@Override
		public void markUsed(int bufIndex) {
			if (bufIndex < 0 || bufIndex >= numBuffers) {
				throw new RuntimeException("bufIndex must be >= 0 and < "
						+ numBuffers);
			}

			if (((usedBits >>> bufIndex) & 0x1) == 0) {
				--numAvailable;
			}
			mark(bufIndex, true);
		}
	}

	private class BitSetAllocatorIndexStrategy extends AllocatorIndexStrategy {

		// We only have this for the case where someone has not called init()
		// before calling any of the mark() methods
		private BitSet bits = new BitSet();

		@Override
		public void init(int numBuffers) {
			super.init(numBuffers);
			bits = new BitSet(numBuffers);
		}

		@Override
		public void clear() {
			numAvailable = numBuffers;
			bits.clear();
		}

		@Override
		public void markFree(int bufIndex) {
			if (bufIndex < 0 || bufIndex >= numBuffers) {
				throw new RuntimeException("bufIndex must be >= 0 and < "
						+ numBuffers);
			}
			if (bits.get(bufIndex)) {
				++numAvailable;
			}
			bits.clear(bufIndex);

			if (availCond != null) {
				availCond.signal();
				availCond = null;
			}
		}

		@Override
		public void markUsed(int bufIndex) {
			if (bufIndex < 0 || bufIndex >= numBuffers) {
				throw new RuntimeException("bufIndex must be >= 0 and < "
						+ numBuffers);
			}
			if (!bits.get(bufIndex)) {
				--numAvailable;
			}
			bits.set(bufIndex);
		}

		@Override
		public int nextFreeIndex() {
			int nextClear = bits.nextClearBit(0);
			if (nextClear >= numBuffers)
				return -1;
			return nextClear;
		}
	}

	private AllocatorIndexStrategy indexer;
	private Lock indexerLock = new ReentrantLock();
	private Condition availCond = indexerLock.newCondition();
	private ByteBuffer masterBuf;
	private ByteBuffer[] buffers;
	private Logger logger = Logger.getLogger(PooledByteBufferAllocator.class);

	public PooledByteBufferAllocator(int numBuffers, int bufSize, boolean direct) {
		if (numBuffers <= 0 || bufSize <= 0) {
			throw new RuntimeException("Invalid parameters");
		}

		if (numBuffers <= PRIMITIVE_MAX_BUFS) {
			this.indexer = new PrimitiveAllocatorIndexStrategy();
		} else {
			this.indexer = new BitSetAllocatorIndexStrategy();
		}

		this.indexer.init(numBuffers);
		if (direct) {
			this.masterBuf = ByteBuffer.allocateDirect(numBuffers * bufSize);
		} else {
			this.masterBuf = ByteBuffer.allocate(numBuffers * bufSize);
		}

		// Allocate the view buffers and initialize
		this.buffers = new ByteBuffer[numBuffers];
		for (int i = 1; i <= numBuffers; ++i) {
			masterBuf.limit(i * bufSize);

			// create a view of the master buffer
			buffers[i - 1] = masterBuf.slice();

			masterBuf.position(i * bufSize);
		}
		masterBuf.rewind();
	}

	public int allocate() throws AllocationException {
		try {
			indexerLock.lock();

			int bufId = indexer.nextFreeIndex();
			if (bufId == -1) {
				throw new AllocationException("No available buffers in pool");
			}
			indexer.markUsed(bufId);
			logger.debug("allocated id: " + bufId);
			return bufId;
		} finally {
			indexerLock.unlock();
		}
	}

	public int allocate(long time, TimeUnit unit) throws AllocationException,
			InterruptedException {
		try {
			indexerLock.lock();

			int bufId = indexer.nextFreeIndex(availCond);
			if (bufId == -1) {
				if (availCond.await(time, unit)) {
					bufId = indexer.nextFreeIndex();
				} else {
					throw new AllocationException(
							"No available buffers in pool");
				}
			}
			indexer.markUsed(bufId);
			logger.debug("allocated id: " + bufId);
			return bufId;
		} finally {
			indexerLock.unlock();
		}
	}

	public void release(int bufId) {
		try {
			indexerLock.lock();
			indexer.markFree(bufId);
			buffers[bufId].clear();
			logger.debug("released id: " + bufId);
		} finally {
			indexerLock.unlock();
		}
	}

	public ByteBuffer retrieve(int id) throws AllocationException {
		if (id < 0 || id >= buffers.length) {
			throw new AllocationException("Invalid buffer ID");
		}
		return buffers[id];
	}

	public static void main(String[] args) throws InterruptedException {
		BasicConfigurator.configure();
		final PooledByteBufferAllocator alloc = new PooledByteBufferAllocator(
				3, 10, false);
		try {
			int buf1Id = alloc.allocate();
			int buf2Id = alloc.allocate();
			int buf3Id = alloc.allocate();

			new Thread(new Runnable() {
				public void run() {
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					alloc.release(2);
				}
			}).start();

			int buf4Id = alloc.allocate(20, TimeUnit.SECONDS);

			buf4Id = alloc.allocate(5, TimeUnit.SECONDS);

			new Thread(new Runnable() {
				public void run() {
					try {
						int buf5Id = alloc.allocate(10, TimeUnit.SECONDS);
						if (buf5Id != -1) {
							alloc.release(buf5Id);
						}
					} catch (AllocationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}).start();
		} catch (AllocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}