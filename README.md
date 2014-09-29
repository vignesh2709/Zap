Zappos coding challenge
=========================

Import this as a complete package in eclipse/netbeans.
I have added the libraries (json simple) in the lib section.
Ignore warnings and proceed with execution.

Coding concept
===============

I have used java and json to connect with the zappos API and have used the given key to retrive the products list.
One I have narrowed down and collected the respective products where i am going to perform my search, I recursively combine 'k' 
products, where 'k' is the number of products specified by the user to check if it sums to 'X', where 'X' is the total price
given by the user.
I also use a treshold of $1 to allow producs within this range (+ or - $1)
