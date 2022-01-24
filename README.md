# reactor-netty client 406 error

On GET requests sometimes servers can respond with 406 with the request contains the content-length.

The problem is that reactor-netty client on HttpClientOperations::newFullBodyMessage adds CONTENT_LENGTH=0

See test cases on RequestTest.java how to work around the problem.

Note: the url used - it is just random URL found in the web.

