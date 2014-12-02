package sorting.polyphase.kway;


import java.util.*;

/**
 * A Standard merge that has, basically, the same API as
 * KWayMerge. Useful to see the difference in the number of comparisons.
 */
public class StandardMerge<T extends KWayMergeIterator<T>>
{
	public StandardMerge()
	{
		fList = new ArrayList<T>();
		fCurrentWinnerIndex = -1;
	}

	public void add(T i)
	{
		fList.add(i);
	}

	public boolean	advance() throws KWayMergeError {
		if ( fCurrentWinnerIndex < 0 )
		{
            for (KWayMergeIterator i : fList)
            {
                local_advance(i);
            }
        }
		else
		{
			KWayMergeIterator winner = fList.get(fCurrentWinnerIndex);
			local_advance(winner);
		}

		if ( !is_done() )
		{
			T		winner = null;
			fCurrentWinnerIndex = -1;
			for ( int j = 0; j < fList.size(); ++j )
			{
				T		i = fList.get(j);
				if ( i.isDone() )
				{
					continue;
				}

				if ( (winner == null) || i.compare(winner).equals(i) )
				{
					winner = i;
					fCurrentWinnerIndex = j;
				}
			}
		}

		return !is_done();
	}

	public boolean		is_done()
	{
		return (fDoneQty >= fList.size());
	}

	public KWayMergeIterator current()
	{
		return fList.get(fCurrentWinnerIndex);
	}

	private void		local_advance(KWayMergeIterator i) throws KWayMergeError {
		i.advance();
		if ( i.isDone() )
		{
			++fDoneQty;
		}
	}

	private List<T>   fList;
	private int       fDoneQty;
	private int       fCurrentWinnerIndex;
}
