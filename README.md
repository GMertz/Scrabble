# Pseudo Code

## Word files goes into a trie
* Can then pass in possible combos to check if they're valid in linear time
## Find the playable spaces
* Iterate through entire board to find each filled space
* Save locations and values of each filled space
## Place a valid word
* For each filled space, try to build the biggest word possible from it (using A*search)
* For each letter in word, check if placing it is valid (based on surrounding letters)
* If not, try new letter/words
* When we run out of letters, decrement target word size
* When a word is found, record word, starting location, and score associated (max heal) and then move on to next space
* Pick word with highest score
