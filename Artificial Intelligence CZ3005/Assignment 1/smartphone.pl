% SumSum and Appy are competitors
competitor(sumsum,appy).

% SumSum develop smartphone technology Galactica_S3
smartphone_tech(galactica_s3).
develop(galactica_s3,sumsum).

% stolen by Stevey who is a boss
boss(stevey).
steal(stevey,galactica_s3,sumsum).

% A competitor of Appy is a rival
rival(X) :- competitor(X,appy).

% Smartphone technology is a business
business(X) :- smartphone_tech(X).

% It is unethical for a Boss to steal business from rival companies
unethical(X) :- boss(X), business(Y), rival(Z), steal(X,Y,Z).
