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

  # warnings and stack traces are expected as long as all tests pass
  make tests

