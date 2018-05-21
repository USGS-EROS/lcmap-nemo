Developing Nemo
===============

Get the Code
------------
.. code-block:: bash

  git clone https://github.com/usgs-eros/lcmap-nemo


Launch Cassandra
-------------------
.. code-block:: bash

  make deps-up

Set up Development Database
---------------------------
.. code-block:: bash

  # development database is configured in profiles.clj
  # The dev profile is used from the repl only
  lein repl
  user=> (require '[lcmap.nemo.setup :as setup])
  user=> (setup/init)

Launch Nemo
-----------
.. code-block:: bash

  # run Nemo from lein
  lein run

  # run Nemo from repl
  lein repl
  user=> (require '[lcmap.nemo.http :as http])
  user=> (mount/start)

Test Nemo
---------
.. code-block:: bash

  # uses the test profile in profiles.clj
  # warnings and stack traces are expected as long as all tests pass
  make tests

Build Nemo
----------
.. code-block:: bash
                
  user@machine:~/lcmap-nemo$ lein uberjar
  Compiling lcmap.nemo.config
  Compiling lcmap.nemo.http
  Compiling lcmap.nemo.util
  Compiling lcmap.nemo.jmx
  Compiling lcmap.nemo.tables
  Compiling lcmap.nemo.db
  Compiling lcmap.nemo.main
  Created /home/user/lcmap-nemo/target/nemo-1.0.0-SNAPSHOT.jar
  Created /home/user/lcmap-nemo/target/nemo-1.0.0-SNAPSHOT-standalone.jar

Run Nemo
--------
See `Running <running.rst/>`_
