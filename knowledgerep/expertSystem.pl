% Define some operators.
% The first number is the precedence
% f is the functor, given by the third argument
% x has strictly lower precedence than f
% y has precedence lower or equal

:- op(900, xfx, ':').
:- op(800, xfx, if).
:- op(600, fx, because).
:- op(550, xfy, or).
:- op(540, xfy, and). % x hasproperty a and x hasproperty b
:- op(200, fx, not).
:- op(600, xfx, implies).
:- op(100, xfx, hasproperty).
:- op(100, xfx, knowsabout).
:- op(100, xfx, linkedwith).
:- op(50, xfy, '&'). % x hasproperty a&b

fact : a and a.
fact : item hasproperty prop.
null implies null.

fact : bob knowsabout walls.

rule : X hasproperty B if X hasproperty A :- A implies B.

rule : X hasproperty B if X hasproperty A and X hasproperty C :- A&C implies B.

fact : A hasproperty B&C :- fact : A hasproperty B, fact : A hasproperty C.

fact : a linkedwith b.

% Symmetric relation
fact : X linkedwith Y :- fact : Y linkedwith X.

% FPCL - failed precondition list
answer(Goal, true, 'specified') :-
	rule: Goal if Premise,
	dealWithConjunction(Premise, true, FPCL).

answer(Goal, false, FPCL) :-
	rule: Goal if Premise,
	dealWithConjunction(Premise, false, FPCL).

answer(Goal, true, specified) :- fact : Goal.

answer(Goal, true, because PC) :- 
	rule: Goal if PC,
	answer(PC, true, _).

dealWithConjunction(A and B, true, []) :-
	dealWithConjunction(A, true, []),
	dealWithConjunction(B, true, []).

dealWithConjunction(A and B, false, FPCL) :-
	dealWithConjunction(A, TA, LA),
	dealWithConjunction(B, TB, LB),
	notBothTrue(TA, TB),
	append(LA, LB, FPCL).

dealWithConjunction(X, true, []) :-
	answer(X, true, _).

dealWithConjunction(X, false, [X]).

notBothTrue(false, _).
notBothTrue(_, false).

answer(Goal, false, because not PC) :-
	rule: Goal if PC,
	answer(PC, false, _).

answer(Goal, false, _).