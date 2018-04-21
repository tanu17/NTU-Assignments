brother(peter, warren).
brother(jerry,kather).
brother(jerry,stuart).
male(stuart).
male(peter).
male(warren).
male(jerry).
female(kather).
female(ann).
female(maryalice).
sister(kather,jerry).
sister(ann, mayalice).
parent_of(maryalice,jerry).
parent_of(warren,jerry).

%Definitions%
father(X,Y):-
    male(X),parent_of(X,Y).
mother(X,Y):-
    female(X),parent_of(X,Y).
son(X,Y):-
    male(X),parent_of(Y,X).
daughter(X,Y):-
    female(X),parent_of(Y,X).
grandfather(X,Y):-
    male(X),father(X,Z),parent_of(Z,Y).
grandmother(X,Y):-
    female(X),mother(X,Z),parent_of(Z,Y).
sibling(X,Y):-
    brother(X,Y);sister(X,Y).
aunt(X,Y):-
    parent_of(Z,Y),sister(X,Z).
aunt(X,Y):-
    mother(X,A),cousin(A,Y).
uncle(X,Y):-
    parent_of(Z,Y),brother(X,Z).
uncle(X,Y):-
    father(X,A),cousin(A,Y).
cousin(X,Y):-
    parent_of(Z,X),sibling(Z,A),parent_of(A,Y).
%assuming spouses must have children%
spouse(X,Y):-
   father(X,A),mother(Y,A).
