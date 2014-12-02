package caching;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import javax.management.MBeanServer;

import org.jgroups.Address;
import org.jgroups.MembershipListener;
import org.jgroups.View;
import org.jgroups.blocks.Cache;
import org.jgroups.blocks.ReplCache;
import org.jgroups.jmx.JmxConfigurator;

public class InitCache {

	private ReplCache<String, String> cache;

	private static final String BASENAME = "replcache";

	private MyTableModel model=null;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		
		
		    String props="udp.xml";
	        String cluster_name="replcache-cluster";
	        long rpc_timeout=1500L, caching_time=30000L;
	        boolean migrate_data=true, use_l1_cache=true;
	        int l1_max_entries=5000, l2_max_entries=-1;
	        long l1_reaping_interval=-1, l2_reaping_interval=30000L;

	        for(int i=0; i < args.length; i++) {
	            if(args[i].equals("-props")) {
	                props=args[++i];
	                continue;
	            }
	            if(args[i].equals("-cluster_name")) {
	                cluster_name=args[++i];
	                continue;
	            }
	            if(args[i].equals("-rpc_timeout")) {
	                rpc_timeout=Long.parseLong(args[++i]);
	                continue;
	            }
	            if(args[i].equals("-caching_time")) {
	                caching_time=Long.parseLong(args[++i]);
	                continue;
	            }
	            if(args[i].equals("-migrate_data")) {
	                migrate_data=Boolean.parseBoolean(args[++i]);
	                continue;
	            }
	            if(args[i].equals("-use_l1_cache")) {
	                use_l1_cache=Boolean.parseBoolean(args[++i]);
	                continue;
	            }
	            if(args[i].equals("-l1_max_entries")) {
	                l1_max_entries=Integer.parseInt(args[++i]);
	                continue;
	            }
	            if(args[i].equals("-l1_reaping_interval")) {
	                l1_reaping_interval=Long.parseLong(args[++i]);
	                continue;
	            }
	            if(args[i].equals("-l2_max_entries")) {
	                l2_max_entries=Integer.parseInt(args[++i]);
	                continue;
	            }
	            if(args[i].equals("-l2_reaping_interval")) {
	                l2_reaping_interval=Long.parseLong(args[++i]);
	                continue;
	            }

	          
	            return;
	        }

	        InitCache initCache = new InitCache();
	        try {
				initCache.start(props, cluster_name, rpc_timeout, caching_time,
				           migrate_data, use_l1_cache, l1_max_entries, l1_reaping_interval,
				           l2_max_entries, l2_reaping_interval);
			} catch (Exception e) {
			
				e.printStackTrace();
			}
	        
	        initCache.statUserInput();
		

	}
	
	private void statUserInput(){
		
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		    int x = 0;
			while (true) {
				System.out.print("Enter Some Text ('x' to exit):");
				String str = br.readLine();
				System.out.println("You just entered:" + str);
				
				
				if ("x".equalsIgnoreCase(str)) {
					System.out.println("Bye!");
					break;
				}else if(str.startsWith("get")){
					String key = str.split("-")[1] ;
					String val =  cache.get( key );
					System.out.println("Value:" + val);
					
				}else{
					System.out.println("incrementing");
					x++;
					this.addtoCache(String.valueOf(x) ,str, Short.valueOf("-1"), 0);
				}
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}

	private void addtoCache(String key, String value, Short repl_count, long timeout){
		
		 cache.put(key, value, repl_count,  timeout );
		
	}
	private void start(String props, String cluster_name, long rpc_timeout,
			long caching_time, boolean migrate_data, boolean use_l1_cache,
			int l1_max_entries, long l1_reaping_interval, int l2_max_entries,
			long l2_reaping_interval) throws Exception {
		
		MBeanServer server = ManagementFactory.getPlatformMBeanServer();

		cache = new ReplCache<String, String>(props, cluster_name);
		
		cache.setCallTimeout(rpc_timeout);
		cache.setCachingTime(caching_time);
		cache.setMigrateData(migrate_data);
		
		JmxConfigurator.register(cache, server, BASENAME + ":name=cache");
		JmxConfigurator.register(cache.getL2Cache(), server, BASENAME + ":name=l2-cache");

		if (use_l1_cache) {
			Cache<String, String> l1_cache = new Cache<String, String>();
			cache.setL1Cache(l1_cache);
			if (l1_reaping_interval > 0)
				l1_cache.enableReaping(l1_reaping_interval);
			if (l1_max_entries > 0)
				l1_cache.setMaxNumberOfEntries(l1_max_entries);
			JmxConfigurator.register(cache.getL1Cache(), server, BASENAME
					+ ":name=l1-cache");
		}

		if (l2_max_entries > 0 || l2_reaping_interval > 0) {
			Cache<String, ReplCache.Value<String>> l2_cache = cache
					.getL2Cache();
			if (l2_max_entries > 0)
				l2_cache.setMaxNumberOfEntries(l2_max_entries);
			if (l2_reaping_interval > 0)
				l2_cache.enableReaping(l2_reaping_interval);
		}

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				cache.stop();
			}
		});

		cache.start();

		model = new MyTableModel<String, String>();
		model.setMap(cache.getL2Cache().getInternalMap());
		cache.addChangeListener(model);

		

		cache.addMembershipListener(new MembershipListener() {
			
			public void viewAccepted(View new_view) {
				System.out.println( "Detaching...." ); 
			}

			public void suspect(Address suspected_mbr) {
			}

			public void block() {
			}

			public void unblock() {
			}
		});
	}
	
	
	  private class MyTableModel<K,V>  implements ReplCache.ChangeListener {
	        private ConcurrentMap<K, Cache.Value<ReplCache.Value<V>>> map;
	        private final String[] columnNames = {"Key", "Value", "Replication Count", "Timeout"};
	        private static final long serialVersionUID=1314724464389654329L;

	        public void setMap(ConcurrentMap<K, Cache.Value<ReplCache.Value<V>>> map) {
	            this.map=map;
	        }

	        public int getColumnCount() {
	            return columnNames.length;
	        }

	        public int getRowCount() {
	            return map.size();
	        }

	        public String getColumnName(int col) {
	            return columnNames[col];
	        }


	        public Object getValueAt(int row, int col) {
	            int count=0;

	            for(Map.Entry<K,Cache.Value<ReplCache.Value<V>>> entry: map.entrySet()) {
	                if(count++ >= row) {
	                    K key=entry.getKey();
	                    Cache.Value<ReplCache.Value<V>> val=entry.getValue();
	                    ReplCache.Value<V> tmp=val.getValue();
	                    switch(col) {
	                        case 0:  return key;
	                        case 1:
	                            V value=tmp.getVal();
	                            return value instanceof byte[]? ((byte[])value).length + " bytes" : value;
	                      
	                        case 2:  return tmp.getReplicationCount();
	                        case 3:  return val.getTimeout();
	                        default: return "n/a";
	                    }
	                }
	            }
	            throw new IllegalArgumentException("row=" + row + ", col=" + col);

	        }

	        public void changed() {
	            
	        	int size = cache.getL2Cache().getSize();
	        	
	        //	System.out.println("\n"+ cache.getL2Cache().getSize() + " elements");
	            
	        	for (int i = 0; i < size; i++) {
					
	        		
						System.out.println( "key=" +  getValueAt(i,0)  + ", value=" + getValueAt(i,1));// + "," + getValueAt(i,2) + "," + getValueAt(i,3)  );
					
				}
	        	
	        }
	    }
	    

	
}
