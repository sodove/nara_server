# NARA - nara is not a rsvpu api 

Simple REST API for RSVPU schedule :shipit:

## How to install
• Install PostgreSQL

• Create new database with next commands: [Click](https://pastebin.com/j5AXzM8Y)

• Change credentials in dotenv

• Compile and use it on your webserver (ready to go build will be available later) :trollface:

## Usage

• How to get groups, teachers, classrooms?

http://servername:port/api/v1/groups/ - groups (?id=value, ?data=value, ?id=value&data=second_value - filter)

http://servername:port/api/v1/preps/ - teachers (same)

http://servername:port/api/v1/auds/ - classrooms (same)

http://servername:port/api/v1/lists/ - all lists at once

Example response: 
>[{"id": 4247,"type": "v_gru","last_update": {"seconds": 1665226325,"nanos": 827868000},"data": "аОП-123"}, {"id": 4540,"type": "v_gru","last_update": {"seconds": 1665226000,"nanos": 827868000},"data": "Ср-321"}] 

• How to get schedule?

http://servername:port/api/v1/schedule/

(?schedulaStyle=boolean - style, ?type=value(&schedulaStyle=boolean), ?type=value&id=value(&schedulaStyle=boolean) - filter)

Example response with schedulaStyle enabled: [Click](https://pastebin.com/ycrj1Uae)

Example response with schedulaStyle disabled: [Click](https://pastebin.com/b3fECj8U)

## Task list
- [X] Lists & Schedule auto updater
- [X] The API functionality is wider and more convenient to use than the University API :trollface:
- [X] Deleting a schedule if it was added more than two weeks ago
- [X] iCal generator
- [X] Do little things in a normal way (at least try)
- [ ] Additional schedule source via parsing university website
- [ ] Main page with statistics
