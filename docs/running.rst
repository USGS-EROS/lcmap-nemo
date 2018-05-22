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

   # use make uberjar or lein uberjar to build
   
   java -jar lcmap-nemo-3.4.0-standalone.jar

   
From Docker:

.. code-block:: bash

   # see https://hub.docker.com/r/usgseros/lcmap-nemo/tags/
   
   docker run -it --rm \
              -e HTTP_PORT=5757 \
              -e DB_HOST=a.cassandra.host \
              -e DB_PORT=9042 \
              -e DB_USER=cassandra_user \
              -e DB_PASS=cassandra_pw \
              -e DB_KEYSPACE=target_keyspace \
              -e DB_TABLES=table1,table2,table3 \
              usgseros/lcmap-nemo:3.4.0
