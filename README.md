Kiln Rally Connector
====================

Kiln Rally Connector (kiln2rally) is a [Kiln][1] web hook to add [Rally][2] integration.  
Similar to Rally Connector for Subversion, it allows you to update the state of Rally items from commit message.

Usage
-----

The connector will attempt to interpret each line of the commit message as one of the following commands:

- DE123 Open
- TA456 Completed
- US789 In-Progress

The states are case sensitive, and they are different for each types of the Rally item. Here's what I have:

    defect_states   = ["Submitted", "Open", "Fixed", "Closed", "Resolved other"]
    task_states     = ["Defined", "In-Progress", "Completed"]
    story_states    = ["Backlog", "Defined", "In-Progress", "Completed", "Accepted"]
 
- DE123 Fixed in 2011.05.20
- DE123 Fixed in 2011.05.20 for Eugene    
- US789 Completed in 2011.05.20 for Eugene

For defects and user stories, you can set Fixed In build and Addressed In build respectively using `in XXX`.
Also, `for Eugene` looks up a user whose name contains "Eugene" and assigns the case to the person.

- TA456 Completed in 14h

For tasks, `in 14h` sets the actual hour spent.

Install
-------

1. Run the follwoing:

       $ sbt assembly
    
2. Grab target/scala_x.x.x/kiln2rally-assembly-x.x.jar and Web.config.
3. Deploy it to some server.
4. Here's `start.bat` for Windows:

       cd E:\Kiln2Rally\
       java -jar E:\Kiln2Rally\kiln2rally-assembly-1.0-SNAPSHOT.jar

License
-------

MIT License.    
    
  [1]: http://www.fogcreek.com/kiln/
  [2]: http://www.rallydev.com/
