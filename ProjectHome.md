EventStore for [Axonframework](http://code.google.com/p/axonframework/), which uses fully transactional (full ACID support ) noSQL database [OrientDB](http://www.orientechnologies.com/).

Preliminary performance tests:

1. 100 threads concurrently writing 1000 events in transactional batches of 10 (resulting in 100 transactions).<br>
2. 100 threads concurrently writing 1000 events in transactional batches of 2 (resulting in 500 smaller transactions).<br>

<b>Orient local database</b><br>
Result: 100 threads concurrently wrote 100 <code>*</code> 10 events each in 29817 milliseconds. That is an average of <b>2585</b> events per second<br>
Result: 100 threads concurrently wrote 500 <code>*</code> 2 events each in 31790 milliseconds. That is an average of <b>3148</b> events per second<br>

<b>Orient remote database with default settings</b><br>
Result: 100 threads concurrently wrote 100 <code>*</code> 10 events each in 29817 milliseconds. That is an average of <b>3354</b> events per second<br>
Result: 100 threads concurrently wrote 500 <code>*</code> 2 events each in 31790 milliseconds. That is an average of <b>3146</b> events per second<br>

<b>File system</b><br>
Result: 100 threads concurrently wrote 100 <code>*</code> 10 events each in 29817 milliseconds. That is an average of <b>8506</b> events per second<br>
Result: 100 threads concurrently wrote 500 <code>*</code> 2 events each in 31790 milliseconds. That is an average of <b>3330</b> events per second<br>

<b>JPA</b><br>
Result: 100 threads concurrently wrote 100 <code>*</code> 10 events each in 29817 milliseconds. That is an average of <b>1582</b> events per second<br>
Result: 100 threads concurrently wrote 500 <code>*</code> 2 events each in 31790 milliseconds. That is an average of <b>946</b> events per second<br>