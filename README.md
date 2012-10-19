RoboModel
=========

A library to simplify database usage on Android.


Quick start
-----------

1. Clone this repository
2. Import it as an eclipse project
3. Add it to your Android project as an Android library
4. In your application, subclass RoboModel and add your persistent data
   as public fields
5. Instantiate a RoboManager instance: `RoboManager<YourModel> manager =
RoboManager.get(context, YourModel.class);`
6. Create an instance of YourModel: `YourModel m = manager.create()`
7. Assign data: `m.answer = 42`
8. Save it: `m.save()`

That's it.


Coming soon
-----------

* Documentation
* Maven integration
* RoboGuice injection
