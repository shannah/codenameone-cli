# codenameone-cli
Command-line tools for [Codename One](http://wwww.codenameone.com)

**Project Status:**  This project is at a very early stage.  If you want to get started with Codename One, building cross-platform mobile apps in Java, I recommend you start with the official [Codename One Getting Started Tutorial](https://www.codenameone.com/download.html).  Once you are familiar with the toolkit, you may want to revisit this command-line tool as it aims to boost development productivity, especially for developers who are comfortable with the command-line.

## Features

### Creating Projects

* Create new Codename One projects for Netbeans, Eclipse, or IntelliJ
* Use any existing CN1 project as a template.
* Select existing template from GUI template browser
* Run templates in Codename One simulator

### Running Apps

* Run Codename One apps in the CN1 simulator

### Distributing Apps

* Easily build and distribute your CN1 applications directly from the command-line

### Project Settings

* Open Codename One settings for projects from the command-line

## Requirements

* [NodeJS](https://nodejs.org/en/) - Only used for distribution of the tool via NPM

## Installation

### Windows

~~~~
npm install -g codenameone-cli
~~~~

### Linux/Mac

~~~~
sudo npm install -g codenameone-cli
~~~~

NOTE: The above commands will install it globally, and you require admin permissions to do this typically (hence the `sudo` in the linux/mac command).  You can also install it locally, using `npm install codenameone-cli` in which case the command will be installed at `./node_modules/.bin/cn1`.

## Usage

### General Usage

~~~~
Usage: codenameone-cli [command]

Commands:
  create - Create a new Codename One Project
  settings - Open project settings for project in current directory.
  css - CSS-related commands
  test - Unit-test related commands
  install-jars - Install latest jars into project
  install-tests - Install tests.xml file with some test targets
  install-appium-tests - Install appium.xml file with some appium tests defined.
  git-init - Same as git init, but adds suitable .gitignore file.
  git-clone - Same as git clone, but installs jars.  
~~~~

### Creating Projects

~~~~
cn1 create <dest-dir> <package-id> <main-class-name> [-i Netbeans|Eclipse|IntelliJ] [-t http://example.com/project/template.zip]
~~~~

#### Examples

**Creating a basic project in directory named "helloworld":**

~~~~
cn1 create helloworld com.mycompany.hello HelloWorld
~~~~

Follow the prompt for the other details (e.g. package id, main class, IDE, template, etc...)

**Using the GUI "create project wizard" ...  Just add `-g` flag:**

~~~~
cn1 create helloworld -g
~~~~

This will open a GUI form that allows you to browse and select a template to use for the project.

![Create project form](https://raw.githubusercontent.com/wiki/shannah/codenameone-cli/images/create-project-form.png)

You can click on any of the templates, and get more details about it, or run the template inside the Codename One simulator directly.

![Template details panel](https://raw.githubusercontent.com/wiki/shannah/codenameone-cli/images/template-details.png)

**Specifying Template and IDE in Command-line**

~~~~
cn1 create helloworld com.mycompany.hello HelloWorld \
    -i Netbeans \
    -t https://github.com/codenameone/SQLSample/archive/master.zip
~~~~

### Opening Project Settings

Navigate to any Codename One project directory, and type:

~~~~
cn1 settings
~~~~

This will open the Codeame One settings app so you can configure your project (e.g. add cn1libs, set up iOS signing, add build-hints etc..

### Contributing Application Templates

The templates listed in the "Create Project Form" are all loaded from [this json file](https://github.com/shannah/codenameone-templates/blob/master/templates.json) hosted on Github. To add your own templates, you can simply fork the [codenameone-templates project](https://github.com/shannah/codenameone-templates), and add your template to that JSON file.  Then issue a pull request.  We'll review it and merge it if appropriate.

Of course, templates don't need to be listed there to be available for use.  If your Codename One project is on Github, you can simply provide the "master.zip" URL as the `-t` parameter of `cn1 create` and the template will be used.

### Cloning Projects From github

Generally, when we host Codename One projects on Github, we strip out the common binaries like CodenameOne.jar, JavaSE.jar, etc...   This can be a little annoying when you are cloning projects because you have to first copy the jar files back into the project before you can build it.  The `cn1 git-clone` command is a thin wrapper around `git clone` that will automatically perform this housekeeping for you.

**Example Usage**

Let's take the [KitchenSink](https://github.com/codenameone/KitchenSink) project, for example.

~~~~
$ cn1 git-clone https://github.com/codenameone/KitchenSink
Cloning into 'KitchenSink'...
Installing jars into KitchenSink...
Downloading 11606508 bytes
Download completed!
Project ready at KitchenSink
~~~~

NOTE: It also works to specify only the project using "ownername/projectname", e.g. `cn1 git-clone codenameone/KitchenSink`

Now we can immediately build this project:

~~~~
$ cd KitchenSink
$ ant jar
~~~~

Or run it in the Codename One simulator:

~~~~
$ ant run
~~~~

Bonus Points:  You can clone and run the project in a single line with:

~~~~
$ cn1 git-clone https://github.com/codenameone/KitchenSink && cd KitchenSink && ant run
~~~~

### Finding Projects To clone

Use the `list-demos` command to find Codename One projects that can be cloned using the `git-clone` command.

**Example**

[source,bash]
----
$ cn1 list-demos
----

You'll get a readout like:

[source,bash]
----
shannah/GeoVizDemo            : A demo app using the Codename One GeoViz Library
----

### Adding Your Own Demos

The `list-demos` command lists all projects on Github that are tagged with both the `codenameone` and `demo` topics.  If you have a demo project that you are hosting on Github, all you need to do is add these topics to your project, and your project will be returned by the `list-demos` command.
