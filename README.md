This is a sample cordapp of Land registry.This cordapp has total three parties (Land Department, Surveyor and Bank) and one Notary as node.

I am using PostgreSQL as a database to store the data. This cordapp template will give you a simple idea on how to creat and store land related data in the database.

Kindly follow the below steps carefully:

1. I have installed PostgreSQL on my system and created three schema and role for the three parties.

2. Deploy the cordapp and before running the nodes update the node.conf file for each parties.

3. Place the Postgre jar file into the drivers path of each party.
