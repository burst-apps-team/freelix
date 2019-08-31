From fuse's XOR Mining explanation - the algorithm behind this was invented by dcct. Another explanation written by jjos can be found [here](https://docs.google.com/document/d/1wgCGt4eD_MNoUaHzxJi8IHLc5ZJu0w-UXqS1jrem1a4/edit).

```
Poc Xor tradeoff

written by f, all credit for finding this technique however goes to dcct

The format n#s# refers to nonce and scoop numbers. Ex n2s3 means scoop 3 of nonce 2.
numScoops is the number of scoops generated from each nonce. This is always 4096 in burst, however numScoops may be used to keep things more general.

If staggered and matched up appropriatly, it is possible to xor 2 completely seperate sets of plot data together, and seperate what is needed back out at run time with a small amount of additional processing, allowing for effectively double mining capacity at the cost of some processing. This is possible by matching up plot data such that the set of stuff you want for a block is xor'ed with a set of stuff which is much easier to generate. The set of stuff you need is always a constant scoop number from many nonces, so that data needs to be stored xored with many scoops of a small number of nonces. This way you can generate a small number of nonces at run time, and for each nonce x, you can xor its data nxs(0-numScoops) with stored data to result in n(y - (y+numScoops))sz for a constant z.

To combine the data appropriately so that each spot in the stored data is useful to 2 different lookups, you xor plot data nxsy with nzsv where (x mod numScoops) == (v mod numScoops), and (y mod numScoops) == (z mod numScoops). In practice, s simple way to get an appropriate matching is stagger the first data as ascending scoops, and the second as ascending nonces. The first set of data then looks like nxs0, nxs1, ... nxs(numScoops - 1), n(x+1)s0, n(x+1)s1, ..., and the second set looks like nys0, n(y+1)s0, ... n(y+numscoops-1)s0, n(y+numscoops)s1, .... This is not not very effecient in regards to hdd seeking, and requires numScoops+1 seeks and reads for everything.

Example plot data. Assume numScoops is only 4, so scoops are numbered 0 - 3.

n0s0 n0s1 n0s2 n0s3 n1s0 n1s1 n1s2 n1s3 n2s0 n2s1 n2s2 n2s3 n3s0 n3s1 n3s2 n3s3
n4s0 n5s0 n6s0 n7s0 n4s1 n5s1 n6s1 n7s1 n4s2 n5s2 n6s2 n7s2 n4s3 n5s3 n6s3 n7s3

Assume items stacked vertically are xored together, so the first position stored on disk is n0s0 xor n4s0.

You can easily see that for any given scoop number in one set of plot data, all of their corresponding parts are different scoops of a single nonce. If you wanted to retrieve all the scoop 1s for example, you can geterate from nonce 5, resulting in n5s(0-3), which can be xored with data read from the disk to result in n(0-3)s1. You could then generate nonce 1, resulting in n1s(0-3), which can be xored with data read from disk to result in n(4-7)s1. You end up with scoop 1 of 8 different nonces while using 4 nonces worth of space, and generating 2 at runtime.

The above example doesn't seem to make a huge difference(only gaining 2 nonces over space and runtime generation), but things get much more efficient when numScoops is larger. Each section of matched up data is always numScoops nonces for each set of plot data, so for example in standard burst you need 4096 nonces for the first plot data, and 4096 nonces for the second set staggered differently. You always need to generate exactly 2 nonces at runtime regardless of what numScoops is to process a section, so for burst numbers you use 4096 nonces worth of space and generate 2 nonces at runtime to retrieve 8192 nonces worth of scoops.

In the naive approach shown above, 1 lookup requires a single seek, and the other lookup requires numScoops seeks. AFAIK there are more effecient ways to rearrange things, but I can't generalize it at the moment. You can interleave multiple sections together with the same concept as a traditional stagger however, so in large scale scenarios even 4097 seeks isn't that bad as that's a constant, not per section.

This is a difficult issue to attempt close. The ability to match up plot data appropriately seems to be more of a property of the system rather than something that can be prevented as long as each nonce generates the same set of scoop numbers.

The simplest known countermeasuer is scoop switching, where you cause users to need different scoops for different nonce ranges. As long as the ranges are smaller than numScoops, a miner doing xor mining needs to do multiple sets of generation instead of just 1 for each section of data, while a normal miner just has to read different data. This does raise the number of required seeks for normal miners however, and generally only multiplies the processing power required by xor miners by a small number.

Another tactic that has been considered is to make nonces generate less than a full set of scoops. This tends to raise the overall complexity a lot, and lower PoW reistance however. Xor mining does end up being more difficult when you get different sets of scoop numbers for different nonces, as things no longer cleanly match up. No quality system for doing this has been designed however.
```