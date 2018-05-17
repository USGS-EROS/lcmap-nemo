.. image:: https://travis-ci.org/USGS-EROS/lcmap-nemo.svg?branch=develop
    :target: https://travis-ci.org/USGS-EROS/lcmap-nemo

Nemo
====
Read Apache Cassandra partitions over HTTP.

Features
--------
* Represents Apache Cassandra partitions as JSON over HTTP
* Works with all supported Cassandra data types
* Simple configuration: keyspace, table list, credentials, hosts and ports
* Dynamic discoverability: tables, partition keys and data
* Built on Clojure 1.9, http-kit, Ring & Compojure

Dynamic Discoverability
-----------------------
Nemo resources create a traversable tree which allows clients
to discover tables, partition keys & partition data.

+-----------------------------+-------------------------------------+
| Resource                    | Result                              |
+=============================+=====================================+
|.. code-block:: ReST         | .. code-block:: javascript          |
|                             |                                     |
|  /                          |   { "tables": ["table1", "table2"] }|
+-----------------------------+-------------------------------------+
|.. code-block:: ReST         | .. code-block:: javascript          |
|                             |                                     |
|  /table1                    |   {                                 |
|                             |     { "key1": 0, "key2": "value1" },|
|                             |     { "key1": 1, "key2": "value2" } |
|                             |   }                                 |
+-----------------------------+-------------------------------------+
|.. code-block:: ReST         | .. code-block:: javascript          |
|                             |                                     |
|   /table1?key1=0&key2=value1|   [{ <partition data> }]            |
+-----------------------------+-------------------------------------+

Documentation (WIP)
-------------------
* Changelog
* Configuration
* Running
* HTTP requests & responses
* Testing & Developing
* CPU and Memory Profiling Results (WIP)
* Limitations (Does not accept maps, sets, lists or blobs as partition keys (is this even valid for Cassandra?)

Running
-------

From a jarfile:

.. code-block:: bash
                
   export HTTP_PORT=5757
   export DB_HOST=a.cassandra.host
   export DB_PORT=9042
   export DB_USER=cassandra_user
   export DB_PASS=cassandra_pw
   export DB_KEYSPACE=target_keyspace
   export DB_TABLES=table1,table2,table3
   
   java -jar lcmap-nemo:1.0

   
From Docker:

.. code-block:: bash

   docker run -it --rm \
              -e HTTP_PORT=5757 \
              -e DB_HOST=a.cassandra.host \
              -e DB_PORT=9042 \
              -e DB_USER=cassandra_user \
              -e DB_PASS=cassandra_pw \
              -e DB_KEYSPACE=target_keyspace \
              -e DB_TABLES=table1,table2,table3 \
              usgseros/lcmap-nemo

Versioning
----------
Nemo follows semantic versioning: http://semver.org/

License
-------
This is free and unencumbered software released into the public domain.

Anyone is free to copy, modify, publish, use, compile, sell, or
distribute this software, either in source code form or as a compiled
binary, for any purpose, commercial or non-commercial, and by any
means.

In jurisdictions that recognize copyright laws, the author or authors
of this software dedicate any and all copyright interest in the
software to the public domain. We make this dedication for the benefit
of the public at large and to the detriment of our heirs and
successors. We intend this dedication to be an overt act of
relinquishment in perpetuity of all present and future rights to this
software under copyright law.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
OTHER DEALINGS IN THE SOFTWARE.

For more information, please refer to http://unlicense.org.
