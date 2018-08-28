Developing Nemo
===============

Get the Code
------------
.. code-block:: bash

  git clone https://github.com/usgs-eros/lcmap-nemo

All releases are merged to master and tagged.

If fixes to a previous version are necessary the tag should be checked out
from master into a topic branch, then merged to a release branch matching the
new version.  The release branch should then be merged into master and tagged as before.

Launch Cassandra
-------------------
.. code-block:: bash

  make deps-up

Set up Development Database
---------------------------
.. code-block:: bash

  # development database is configured in project.clj :repl profile
  # may override with profiles.clj 
  lein repl
  user=> (require '[lcmap.nemo.setup :as setup])
  user=> (setup/init)
  # you may also set up nemo_dev keyspace & tables like so
  user=> (init)

Launch Nemo
-----------
.. code-block:: bash

  # run Nemo from lein
  lein run

  # run Nemo from repl 
  lein repl
  user=> (start)

  # stop Nemo if necessary
  user=> (stop)
  
The repl will automatically import most Nemo namespaces using short aliases

See `user.clj <../dev/user.clj/>`_
  
Test Nemo
---------
.. code-block:: bash

  # uses the test profile in project.clj
  # warnings and stack traces are expected as long as all tests pass
  make tests

Build Nemo
----------
.. code-block:: bash

  # may also use make uberjar
  
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

Deploy to Dockerhub
-------------------
Travis-CI automatically builds each commit and pushes a built Docker image to Dockerhub tagged with the version and branchname.

To deploy manually, see the `Makefile <../Makefile/>`_.
