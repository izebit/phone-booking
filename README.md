# Phone Booking

![main-page](https://github.com/izebit/phone-booking/blob/master/pics/main-page.png?raw=true)
## Build
```shell
docker build -t phone-booking .
```

## Run
```shell
docker run -p 8080:8080 phone-booking
```
and then go to http://localhost:8080/booking/phones   
To login, you can create a new user or user existing one with credentials **TestUser/test**. 