This is a sample cordapp of IOU template. I have total two parties (Party A and Party B) and one Notary as node.

I am using PostgreSQL as a database to store the data. This cordapp template will give you a simple idea on how to create a cordapp and store the data in the database.

Kindly follow the below steps carefully:

1. I have installed PostgreSQL on my system and created two schema and role for two parties (Party A and Party B).

2. Deploy the cordapp and before running the nodes update the node.conf file for each parties.

3. Place the Postgre jar file into the drivers path of each party.