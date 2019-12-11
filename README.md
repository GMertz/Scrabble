# Pseudo Code
1. Use a trie to store all of the words in enable1.txt. 
   - This allows us to quickly find words that fit a search template (of the form “\_a\_\_” where blanks will be filled with letters from our hand) 
   - Using the Trie, along with some specific rules on our search, allows us to avoid many false alarms that would occur through straight brute forcing (simply trying all combinations). 
2. Searcher uses the Trie to formulate all possible words that can be generated from a given template t, with a few constraints.
   1. The template must contain at least one letter that is adjacent to a tile already placed on the board, 
   2. The tile after our word must a blank space. 

3. We search by considering an entire row as a ‘window’. We attempt to create a template from each valid spot in this window, and consider a window valid to be searched if
   1. It contains at least one letter that is adjacent to an already placed tile
   2. The letter before the start of the window is a blank. 
   
4. Once we have finished conducting this row by row search, we do the same thing column by column, all the while storing each found word in an PriorityQueue (sorting the words by the score that would be gained by placing the word on the board, also a note: this could be replaced with an array which is sorted retrospectively for probable performance increase).

5. With all the words gathered, we start furiously polling them from the queue until we find one that the gatekeeper will accept. 
If we don’t find such a word, we do a smart Exchange, which prioritizes returning non-vowel duplicate letters.
