Running
=======

From a jarfile:

.. code-block:: bash
                
   export HTTP_PORT=5757
   export DB_HOST=a.cassandra.host
   export DB_PORT=9042
   export DB_USER=cassandra_user
   export DB_PASS=cassandra_pw
   export DB_KEYSPACE=target_keyspace
   export DB_TABLES=table1,table2,table3
   export DB_CONNECT_TIMEOUT_MILLIS=30000
   export DB_READ_TIMEOUT_MILLIS=600000

   # use make uberjar or lein uberjar to build
   
   java -jar lcmap-nemo-3.4.1-standalone.jar

   
From Docker:

.. code-block:: bash

   # see https://hub.docker.com/r/usgseros/lcmap-nemo/tags/
   
   docker run -it --rm -p 5757:5757 \
              -e HTTP_PORT=5757 \
              -e DB_HOST=a.cassandra.host \
              -e DB_PORT=9042 \
              -e DB_USER=cassandra_user \
              -e DB_PASS=cassandra_pw \
              -e DB_KEYSPACE=target_keyspace \
              -e DB_TABLES=table1,table2,table3 \
	      -e DB_CONNECT_TIMEOUT_MILLIS=30000 \
	      -e DB_READ_TIMEOUT_MILLIS=600000 \
              usgseros/lcmap-nemo:3.4.1
