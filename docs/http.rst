HTTP Requests and Responses
===========================

Example HTTP requests & responses for Nemo running against test database.

Table and column descriptions are in `setup.clj <../test/lcmap/nemo/setup.clj/>`_.

.. code-block:: bash

  #-----------------------------------------
  # show available tables
  #-----------------------------------------
  
  user@machine:~$ http http://localhost:5757
  HTTP/1.1 200 OK
  Content-Length: 24
  Content-Type: application/json; charset=utf-8
  Date: Thu, 17 May 2018 21:01:18 GMT
  Server: http-kit

  {
      "tables": [
          "one", 
          "two"
      ]
  }


  #-----------------------------------------
  # partition keys for table "one"
  #-----------------------------------------
   
  user@machine:~$ http http://localhost:5757/one
  HTTP/1.1 200 OK
  Content-Length: 13132
  Content-Type: application/json; charset=utf-8
  Date: Thu, 17 May 2018 21:03:21 GMT
  Server: http-kit

  [
      {
          "pk1": 1897175, 
          "pk2": 1203
      }, 
      {
          "pk1": -21458353, 
          "pk2": -19279865
      }, 
      {
          "pk1": 502152, 
          "pk2": -47
      }, 
      {
          "pk1": 24621077, 
          "pk2": 1
      }
  ]


  #-----------------------------------------
  # undefined tables issue HTTP 404
  #-----------------------------------------
  
  user@machine:~$ http http://localhost:5757/undefined-table
  HTTP/1.1 404 Not Found
  Content-Length: 18
  Content-Type: application/json; charset=utf-8
  Date: Thu, 17 May 2018 21:13:50 GMT
  Server: http-kit

  [
    "undefined-table not found"
  ]

  
  #-----------------------------------------
  # partition data
  #-----------------------------------------

  user@machine:~$ http http://localhost:5757/one pk1==1897175 pk2==1203
  HTTP/1.1 200 OK
  Content-Length: 1435
  Content-Type: application/json; charset=utf-8
  Date: Thu, 17 May 2018 21:05:14 GMT
  Server: http-kit

   [
      {
        "f1": "0x1+RriCdHS6iVnsaZk=", 
        "f10": "7d71a810-373e-4d64-8813-8736237da998", 
        "f11": "8Dvj833b4", 
        "f12": 6, 
        "f13": [
            "K", 
            "7WUv6zPRV9xeNj0dS2Icqw2oZ5Qk", 
            "wHnf1DGzQycQbLW", 
            "eFwDkkXY2Mwd9eJJz9feCnhj3Mb", 
            "16EfHrGWpDbD9v", 
            "pjf9541FF", 
            "F4h66", 
            "4sUWWND9uOpVtT49JeKwQ", 
            "g410SbmK5Z41plc2i1laUs5", 
            "q01kS8Kg8d455cA56Ii2c", 
            "LEWhqQ2RP7C09U7JC0yvVTg", 
            "1YYGzA2lPiojKdrY9cAJyi0", 
            "13g6Bn9sKgGqh", 
            "UN5Nv3"
        ], 
        "f14": {
            "1s4StwYDPy20yoo": "T1yS524gs9DVcwbXGum", 
            "8wRQqqelPpwKZHng766297383q64Wu": "E47bDpZynxMy549UlOUU42O9", 
            "Az6vZwUOq51KmjyTEPMfb": "O70wnPXptGfz6D13K3Yj", 
            "DRSIL2w7D2xZn9x5Dm6": "7fgdWYRMTm492S050zXXkOyI3773J5", 
            "Dt": "iE2iA3Dl486R", 
            "FEsmNFSe3t": "60m34Un9klP6960Uiu6PrMv", 
            "O4wiE5x263P838Z8S5l6hHsXlurSj": "e9o8BLUJV8F2AE6VT1tPMrKwH", 
            "Vvc72Icah9O4mYxNSw0lsGO": "hMhKpB9e8ifU1BV9CQnef98j", 
            "q63FTFJ": "GVOHYIIspgQ", 
            "v": "YNL3J5f0X7", 
            "vv9LLk3T": "hJo11"
        }, 
        "f15": [
            "4O7ChaYB3921s8hbC3L", 
            "zl67U3M9KmyQdWmAKv0S11M079", 
            "6S6z7X3Y", 
            "c3Bet6", 
            "Aq9i1V9SzF478b1VbM29Ojco5P", 
            "0al2K9hRU01sw10K8H314L", 
            "bJqg5Y7", 
            "4Z29G", 
            "7WreXk6U42fR6c", 
            "LkH6Izx8rg", 
            "E14", 
            "3GkouKP25Sw0i86xt9w82MViCFQ93", 
            "rJJ26Wb8BI25Ap79v0Z5jK6oieay", 
            "0e0EIE0KUQ9VK2mnFDc", 
            "f7oUS30PWfvq7R", 
            "n08GA8N", 
            "MR59vpgTono8X118209PKru", 
            "56wUG372i4ttpncF64qiJ29d", 
            "e301j210y4T", 
            "cB18u1C2e34LLWmge"
        ], 
        "f2": "Lota9aTn902qeKk64AO4sq7v", 
        "f3": -0.0, 
        "f4": 8.0, 
        "f5": "U*u -]_U>I29[h'{\"(}}ji>r", 
        "f6": true, 
        "f7": -777510, 
        "f8": "221.1.187.166", 
        "f9": "8eb3fbb0-593c-11e8-b272-7d7360c2bcf3", 
        "pk1": 1897175, 
        "pk2": 1203, 
        "pk3": "1970-01-01T00:22:47Z"
    }
  ]

  #-----------------------------------------
  # Zero results for partition keys
  #-----------------------------------------
  
  user@machine:~$ http http://localhost:5757/one pk1==1897175 pk2==12033
  HTTP/1.1 200 OK
  Content-Length: 2
  Content-Type: application/json; charset=utf-8
  Date: Thu, 17 May 2018 21:16:04 GMT
  Server: http-kit

  []


  #-----------------------------------------
  # Example parameter coercion failure
  #-----------------------------------------
  
  david@dev:~$ http http://localhost:5757/one pk1==1897175 pk2==not-a-number
  HTTP/1.1 500 Internal Server Error
  Content-Length: 33
  Date: Thu, 17 May 2018 21:17:00 GMT
  Server: http-kit

  not-a-number cannot be coerced to :bigint
