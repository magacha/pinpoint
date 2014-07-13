Pinpoint : find casues for effects
====================================

This android app can be used when you are trying to 
find the causes of a given effect. This is 
particularly useful when you have many options
to choose among the possible causes.

For example you can use it to:

  - Find which foods you can't digest
  - Find what makes you sleep badly

Every time you observe the effect you enter it
into the application. Then you enter all the 
possible causes up to 24 hours before. Then for
each of these causes you enter them every time
they happen. If the effect happens again you,
repeat the procedure. Each time potentially 
increasing the number of possible causes that
you track. After a while the program will
pinpoint which one is more likely to occour
just before the effect.

The appliction allows you to enter a sequence of 
event  occurences in the form of:

event, time

and compute the most probable causes of any of
them.

The probable causes are computed as follows:

For every occurrence of the effect the program
finds all the other events that happened up to
24h before. These are the possible causes.

To rank the causes the program counts the times
the a given event happened up to 24 hours before
the effect and divides it by the number of 
occurences of the event.


