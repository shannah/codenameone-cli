# codenameone-cli
Command-line tools for [Codename One](http://wwww.codenameone.com)

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

## Usage

### Creating Projects

~~~~
cn1 create <dest-dir> <package-id> <main-class-name> [-i Netbeans|Eclipse|IntelliJ] [-t http://example.com/project/template.zip]
~~~~

**Examples**

Creating a basic project in directory named "helloworld":

~~~~
cn1 create helloworld
~~~~

Follow the prompt for the other details (e.g. package id, main class, IDE, template, etc...)

Using the GUI "create project wizard" ...  Just add `-g` flag:

~~~~
cn1 create helloworld -g
~~~~

This will open a GUI form that allows you to browse and select a template to use for the project.

![Create project form](https://raw.githubusercontent.com/wiki/shannah/codenameone-cli/images/create-project-form.png)


