%Relationships in the royal family%
male(princecharles).
male(princeandrew).
male(princeedward).
female(princessann).
female(queenelizabeth).
sister(princessann,princecharles).
sister(princessann,princeandrew).
sister(princessann,princeedward).
brother(princecharles,princessann).
brother(princeedward,princessann).
brother(princeandrew,princessann).
mother(queenelizabeth,princecharles).
mother(queenelizabeth,princeandrew).
mother(queenelizabeth,princeedward).
mother(queenelizabeth,princessann).
child(princecharles, queenelizabeth).
child(princessann, queenelizabeth).
child(princeandrew, queenelizabeth).
child(princeedward, queenelizabeth).
older_than(princecharles, princessann).
older_than(princecharles, princeandrew).
older_than(princecharles, princeedward).
older_than(prnicessann, princeandrew).
older_than(princessann,princeedward).
older_than(princeandrew, princeedward).

%Rules%
sister(X,Y):-female(X),mother(A,Y),mother(A,X).
brother(X,Y):-male(X),mother(A,Y),mother(A,X).

%Old Royal succession rule - respective of gender%
precedes(X,Y):- male(X), male(Y), older_than(X,Y).
precedes(X,Y):- male(X), female(Y), Y\=elizabeth.
precedes(X,Y):- female(X), female(Y), older_than(X,Y).

succession_sort([A|B], Sorted) :- succession_sort(B, SortedTail), insert(A, SortedTail, Sorted).
succession_sort([], []).

insert(A, [B|C], [B|D]) :- not(precedes(A,B)), !, insert(A,C,D).
insert(A, C, [A|C]).

successionList(X, SuccessionList):-findall(Y, child(Y,X), Children),succession_sort(Children, SuccessionList).

%New Royal succession rule - irrespective of gender%
successor(X,Y):-child(Y,X).

successionListIndependent(X, SuccessionList):-findall(Y,successor(X,Y),SuccessionList).
