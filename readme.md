# Api documentation



## Values
- **[Member](https://github.com/AlexC-ux/encryshareweb/edit/main/README.md#member)**
- - [Member.**act**.Values](https://github.com/AlexC-ux/encryshareweb/edit/main/README.md#memberactvalues)
- **[Data](https://github.com/AlexC-ux/encryshareweb/edit/main/README.md#data)**
- - [Data.**type**.Values](https://github.com/AlexC-ux/encryshareweb/edit/main/README.md#datatypevalues)
- **[ErrorCodes](https://github.com/AlexC-ux/encryshareweb/edit/main/README.md#errors)**


## Methods
- **[Member](https://github.com/AlexC-ux/encryshareweb/edit/main/README.md#member)**
- - [Register](https://github.com/AlexC-ux/encryshareweb/edit/main/README.md#registration)
- - [GetInfo](https://github.com/AlexC-ux/encryshareweb/edit/main/README.md#getinfo)
- - [GetPublicRSAkey](https://github.com/AlexC-ux/encryshareweb/edit/main/README.md#getpublicrsakey)
- **[Data](https://github.com/AlexC-ux/encryshareweb/edit/main/README.md#data)**
- - [SendData](https://github.com/AlexC-ux/encryshareweb/edit/main/README.md#senddata)
- - [ReceiveData](https://github.com/AlexC-ux/encryshareweb/edit/main/README.md#receivedata)
- [**Chat**](https://github.com/AlexC-ux/encryshareweb/edit/main/README.md#chat)
- - [CreateChat](https://github.com/AlexC-ux/encryshareweb/edit/main/README.md#createchat)
- - [GetChatInfo](https://github.com/AlexC-ux/encryshareweb/edit/main/README.md#getchatinfo)
- - [AddMember](https://github.com/AlexC-ux/encryshareweb/edit/main/README.md#addmember)
- - [RemoveMember](https://github.com/AlexC-ux/encryshareweb/edit/main/README.md#removemember)
- - [GetChats](https://github.com/AlexC-ux/encryshareweb/edit/main/README.md#getchats)


___
## Member

### Registration
**Register a new profile**

*path: /api/reg.php*


#### Registration.Parameters
| Parameter      | Values           | Description                                                                 |
|-----------     |---------------   | ----------------------------------------------------                        |
| ``` act ```           |  [enum](https://github.com/AlexC-ux/encryshareweb/edit/main/README.md#registrationactvalues)            | Set this parameter = ``` reg ```                                            |
| ``` name ```           |  string(70)      | Member's name whitch will be shown. *70 characters or less*                 |
| ``` login ```          |  string(100)     | Member's login whitch needed for authorization. *100 characters or less*    |
| ``` pswd_hash ```      |  string(172)     | Password encrypted with [RSA encryption key](https://github.com/AlexC-ux/encryshareweb/edit/main/README.md#getpublicrsakey). *172 characters or less* (password must be 117characters or less)|

>**After successful registration, the server returns a JSON object with the ``` api_key ``` field in the response.**
>{"api_key":"$2y$10$U9B5yRywbx3YCCO0d13kpOIfDG28tBD3uASqU.WW1BNqAr5fZ2YUi"}

___
### GetInfo
**Get profile's information**

*path: /api/reg.php*


#### GetInfo.Parameters
| Parameter      | Values           | Description                                                                 |
|-----------     |---------------   | ----------------------------------------------------                        |
| ``` act ```            |  [enum](https://github.com/AlexC-ux/encryshareweb/edit/main/README.md#memberactvalues)            | Set this parameter = ``` login ```                                          |
| ``` api_key ```        |  string(60)      | Api key which you can get after [registration](https://github.com/AlexC-ux/encryshareweb/edit/main/README.md#registration) *60 characters long* |


>**After successful authorization, the server returns a JSON object with the fields ``` name ```, ``` login ```, ``` id ```**
>{"0":"8","id":"8","1":"name","name":"name","2":"login","login":"login"}

___
### GetPublicRSAkey
**Get RSA public key which is needed to encrypt the password**

*path: /api/reg.php*

#### GetPublicRSAkey.Parameters
| Parameter      | Values           | Description                                                                 |
|-----------     |---------------   | ----------------------------------------------------                        |
| ``` act ```            |  [enum](https://github.com/AlexC-ux/encryshareweb/edit/main/README.md#memberactvalues)            | Set this parameter = ``` pubkey ```                                         |

>**After a request, the server returns a JSON object with the ``` pubkey ``` field in the response.**
>
>{"pubkey":"-----BEGIN PUBLIC KEY-----\r\nMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCZxskVfb8omSqhiRCC79T\/R5+O\r\nYQwKWeFh58TxDu38RXRh59zPEvGYCk8PuA2gbpwSS\/yUMGrUCPsC1nN1sM1ZaR4C\r\njOlPZZEnY59kJLnupr0AwnIJpIwcohjBJAisnu3HKZKjGvccMXsh6A\/46Yq6xCzd\r\np140YOHjLlerDnIT9QIDAQAB\r\n-----END PUBLIC KEY-----"}
>
>**``` \r\n ``` must be replaced by the line break character**

___

#### Member.act.Values
| Values                    | Description                                                                 |
|---------------            | ----------------------------------------------------                        |
| ``` reg ```               | Used when registering a new member                                          |
| ``` login ```             | Used to retrieve the data of a registered member                            |
| ``` pubkey ```            | Used to obtain the public RSA encryption key                                |

___
## Data

### SendData
**Sending data to temporary server storage**

*path: /api/sData.php*

#### SendData.Parameters
| Parameter      | Values           | Description                                                                 |
|-----------     |---------------   | ----------------------------------------------------                        |
| ``` type ```           |  [enum](https://github.com/AlexC-ux/encryshareweb/edit/main/README.md#datatypevalues)          | This parameter will tell the recipient what data you are sending            |
| ``` api_key ```        |  string(60)      | Api key which you can get after [registration](https://github.com/AlexC-ux/encryshareweb/edit/main/README.md#registration) *60 characters long* |
| ``` recepient ```      | string(70)       | Recepient's id                                                              |
| ``` data ```           | Values           | Data to send                                                                |


>**After a request, the server returns a JSON object with the [``` ErrorCode ```](https://github.com/AlexC-ux/encryshareweb/edit/main/README.md#errors).**
>
>{"Error":"Successfully!","ErrorCode":0}

___
### ReceiveData
**Receiving data from the temporary server storage**

*path: /api/rData.php*

#### ReceiveData.Parameters
| Parameter      | Values           | Description                                                                 |
|-----------     |---------------   | ----------------------------------------------------                        |
| ``` api_key ```        |  string(60)      | Api key which you can get after [registration](https://github.com/AlexC-ux/encryshareweb/edit/main/README.md#registration) *60 characters long* |



>**After a request, the server returns a JSON object with the [``` ErrorCode ```](https://github.com/AlexC-ux/encryshareweb/edit/main/README.md#errors)**
>
>{"Error":"Successfully!","ErrorCode":0}

**or**

>**After a request, the server returns a JSON object with the [``` type ```](https://github.com/AlexC-ux/encryshareweb/edit/main/README.md#datatypevalues), ``` recepient ```, ``` senderId ``` and ``` message ``` fields**
>
>{"0":"18","id":"18","1":"TYPEOFDATA","type":"TYPEOFDATA","2":[{\"senderName\":\"name\",\"senderId\":\"8\",\"data\":\"hello1\"}],"data":[{\"senderName\":\"name\",\"senderId\":\"8\",\"message\":\"hello1\"}],"3":"9","recepient":"9"}

___
#### Data.type.Values
| Value                       | Description                                                                    |
|---------------              | ----------------------------------------------------                           |
| ``` request ```             | JSON object with ``` RSAopenKey ``` (sender's personal open key) and ``` senderId ``` (sender's id) fields.                 |
| ``` AES_data ```            | JSON object with ``` senderId ``` (sender's id), ``` AES_IV ```  and ``` AES_key ```    fields |
| ``` data ```                | Encrypted (by aes) JSON object with ``` senderId ``` (sender's id) and ``` data ```     fields |

___
## Chat

### CreateChat
**Chat creating**

*path: /api/createChat.php*

| Parameter      | Values           | Description                                                                 |
|-----------     |---------------   | ----------------------------------------------------                        |
| ``` api_key ```        |  string(60)      | Api key which you can get after [registration](https://github.com/AlexC-ux/encryshareweb/edit/main/README.md#registration) *60 characters long* |
| ``` chat_name ```      | string(150) | The name of the chat to be created                                       |

>**After a request, the server returns a JSON object with the [``` ErrorCode ```](https://github.com/AlexC-ux/encryshareweb/edit/main/README.md#errors).**
>
>{"Error":"Successfully!","ErrorCode":0}

___
### GetChatInfo
**Getting information about chat**

*path: /api/getChatInfo.php*

| Parameter      | Values           | Description                                                                 |
|-----------     |---------------   | ----------------------------------------------------                        |
| ``` api_key ```        |  string(60)      | Api key which you can get after [registration](https://github.com/AlexC-ux/encryshareweb/edit/main/README.md#registration) *60 characters long* |
| ``` chat_id ```      | string(60) | Id of the chat                                                              |

>{"0":"2","chat_id":"2","1":null,"chat_avatar":null,"2":"hellow2","chat_name":"hellow2","3":"1","chat_owner":"1","members":[{"0":"1","member_id":"1"},{"0":"10","member_id":"10"},{"0":"12","member_id":"12"}]}

___
### AddMember
**Adding a member to the chat**

*path: /api/addMember.php*

| Parameter      | Values           | Description                                                                 |
|-----------     |---------------   | ----------------------------------------------------                        |
| ``` api_key ```        |  string(60)      | Api key which you can get after [registration](https://github.com/AlexC-ux/encryshareweb/edit/main/README.md#registration) *60 characters long* |
| ``` chat_id ```      | string(60) | Id of the chat                                                              |
| ``` member_id ```      | string(60) | Id of the user to be added                                                |

>**After a request, the server returns a JSON object with the [``` ErrorCode ```](https://github.com/AlexC-ux/encryshareweb/edit/main/README.md#errors).**
>
>{"Error":"Successfully!","ErrorCode":0}

___
### RemoveMember
**Removing a member from the chat**

| Parameter      | Values           | Description                                                                    |
|-----------             |---------------   | ----------------------------------------------------                   |
| ``` api_key ```        |  string(60)      | Api key which you can get after [registration](https://github.com/AlexC-ux/encryshareweb/edit/main/README.md#registration) *60 characters long* |
| ``` chat_id ```        | string(60)       | Id of the chat                                                         |
| ``` member_id ```      | string(60)       | Id of the user to be added                                             |
| ``` remove ```         | string(60)       | Parameter without value                                                |

>**After a request, the server returns a JSON object with the [``` ErrorCode ```](https://github.com/AlexC-ux/encryshareweb/edit/main/README.md#errors).**
>
>{"Error":"Successfully!","ErrorCode":0}

___
### GetChats
**Obtaining a list of user chats**

*path: /api/getChats.php*

| Parameter      | Values           | Description                                                                 |
|-----------     |---------------   | ----------------------------------------------------                        |
| ``` api_key ```        |  string(60)      | Api key which you can get after [registration](https://github.com/AlexC-ux/encryshareweb/edit/main/README.md#registration) *60 characters long* |

>{"Chats":[{"0":"14","chat_id":"14"},{"0":"15","chat_id":"15"}]}

___
## Errors
| Error code     | Description                                                                    |
|-----------     | ----------------------------------------------------                           |
| 0              | Successfully (not error)                                                       |
| -1             | Unknown Error                                                                  |
| 11             | Appears when trying to log on with an invalid password                         |
| 12             | Appears when trying to log on with an invalid login                            |
| 17             | Appears when trying to register a new member in case the name is already taken |
| 18             | Appears when trying to use nonexistent [member_id](https://github.com/AlexC-ux/encryshareweb/edit/main/README.md#getinfo) |
| 30             | Error during chat creation                                                     |
| 31             | Appears when attempting a query with an invalid api key                        |
| 404            | Appears in response to an incorrectly sent request.                            |
