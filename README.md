# Custom Code Folding Eclipse Plugin
This plugin enhances the default code folding abilities of all text based Eclipse editors supporting code foldings (e.g. Java, XML, Generic Text Editor, ...).
Following two options are provided:

1. Wrap the code block via the two comments ```#region``` to specify the start line and ```#endregion``` to specify the end line.
    1. Java
        ```java
        // #region main method
        public static void main(String args[])
        {
            // #region if block
            for (String arg : args) {
                System.out.println(arg);
            }
            // #endregion
        }
        // #endregion
        ```
    2. XML
        ```xml
        <templates>
            <!-- #region first -->
            <template 
                id="template.1" name="first" 
                description="first template">
            </template>
            <!-- #endregion -->
        </templates>
        ```
2. Mark the to be folded code block and run the command 

    ```Create Custom Code Folding for Selection and Collapse``` 

    via the Ctrl-3 dialog. 

    Custom created foldings can be deleted via the commands 
    
    ```Delete All Custom Code Foldings in Active Editor``` and 
    ```Delete Custom Code Folding at Cursor```
