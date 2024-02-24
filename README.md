# SwingTestingToolkit
Java Swing UI testing toolkit

Learn basics [here](swing-testing-toolkit/unpackaged-src/BasicsExample.java ).

Learn how to extend [here](swing-testing-toolkit/src/test/java/xy/ui/testing/TestExtensibility.java ).

![alt screenshot](https://raw.githubusercontent.com/dotxyteam/SwingTestingToolkit/master/swing-testing-toolkit/misc/screenshots/specification4.png)

Swing Testing Toolkit is a Java-Swing GUI testing framework. It uses a semi-automatic test case generation approach aimed at reducing testing instructions to the bare minimum while ensuring that they are as relevant as possible.

It is not a universal GUI testing framework. It focuses on a single library (Swing) to guarantee its level of quality.

## Videos
- [Testing an ATM simulator](https://www.youtube.com/watch?v=6NAyWJDkM24)
- [Inspecting a GUI](https://www.youtube.com/watch?v=RMxSnS-WdzQ)
- [Getting started](https://www.youtube.com/watch?v=MYihZoEOSGA)

## Features
- Test case editor (record/edit/replay component finders and test actions)
- Test report view (logs, screenshots, …)
- Components inspector
- XML-based specification
- Various robust component finders
- Compatible with JUnit
- Extensible (can easily add custom test actions and component finders)

## Requirements
Tested on Java  1.8 and 17

## Usage

    Use Maven:
        <dependency>
        <groupId>com.github.dotxyteam</groupId>
        <artifactId>swing-testing-toolkit</artifactId>
        <version>1.8.9</version>
        </dependency>
    Or download the “*dist*.zip” package (see the download section below)
    -    Extract it
    -    Include “target” and “target/dependency” directories jars in your classpath
    Run the main class “xy.ui.testing.editor.TestEditor”
    Read the user guide

## Download
Binaries and sources archive are available [here](https://github.com/dotxyteam/SwingTestingToolkit/releases).

# Creating a Test Specification

**Tests specifications**  are a convenient way to organize a suite of actions and assertions that make up a test case. They are saved to the disk as XML files (convenient and standardized storage format).

Tests specifications are primarily composed of actions and assertions.

**Actions** are things that a user usually does to operate a GUI component, like pushing a button, selecting a menu item, or entering a text. Most actions require one or more arguments.

**Assertions** (Check …) allow you to verify the state of the GUI. You can check the value of a component property, check visible strings, … .

Normally the test will stop running and report an error if actions or assertions fail.

**Component finders** represent actual GUI components within your code under test. They are used by most actions and assertions to locate the target GUI component.

## Create New Test Specification

Run the class “xy.ui.testing.editor.TestEditor”.

## Open an Existing Test Specification

Select **Open Specification** from the **File** menu, and you can browse for an existing test specification to open. The filename extension is “stt“.


# Launching an Application

The **Call Main Method** action allows you to launch your code under test by executing its main class. Of course this class must be in the classpath and have a “main” method that is public and static.

Note that the **Call Main Method** action is threaded. You can setup this launch step to fail if an exception is thrown. See the option: “Check exceptions after (seconds)”.


# What goes into a Test Specification ?

The **Add/Insert** button on the action list toolbar allows you to manually insert any type of test action/assertion.

## Recording/Playback

The easiest way to see the types of things that go into a test specification is to record. You can start recording by clicking on “**Record…**“. Recording settings will then appear.  If your application has not been launched yet, you can click on the checkbox “**Start By Calling Main Method**” and provide the main class name there. You can then click on Record and the recording will begin.

Note that the recording does not cause the capture of all events. You must explicitly choose the component with which you want to interact by left-clicking on it and then select the type of interaction (action or assertion) in the dialog box that will open. This greatly reduces the complexity/maintainability of the test cases.

When recording, the selection of suggested actions/assertions may change based on the current component. The dialog box of the suggested actions/assertions is dynamically filled by the used **Tester** class instance.  These actions/assertions are automatically configured as much as possible according to the selected component state. Once the action/assertion is inserted, you can further edit its attributes in the editor.

When you are finished recording, you must click on the red  stop button to stop recording. Then you can click on the **Replay All** button to play back the recording.


# Advanced Topics

## Integrate with JUnit

The test specifications can be executed using the following method:

**TestingUtils.assertSuccessfulReplay(new File(“path/to/test-specification.stt”));**

Note that this method ends by closing all the testable windows to prepare the environment for another replay.

## System.exit() call interception

The tested application and the test code share the same JVM. This is why it is possible to prevent the tested application from shutting down the JVM after a test action execution. For this purpose you can use the “****System exit call interception****” action.
