<!-- --- title: GitHub overview -->

This project extends the [Pikater](https://github.com/peskk3am/pikater4) project with multi-user support, web based GUI and distributed infrastructure to allow simultaneous computation of many individual tasks.

As such, this application consists of 3 main parts:

1. **Core system**  
The original pikater project. Although it has been rewritten to support the extension, it still works as a standalone unit.
2. **Database framework**
3. **Web application extension**

The project also contains an inline documentation written in markdown, easily available as a wiki.
More information on each of these "components" can be found in the [documentation](#docs).




## Main features

Client:
* 2D editor to define experiments (boxes and edges style).
* Potentially many experiments may be scheduled to execution.
* Displaying the experiment results and converting/downloading them into a human-readable format, such as CSV.
* Uploading custom data sets and computation methods.

Server:
* Many features of the original Pikater project, such as computation method recommendation.
* Experiments planning and execution.
* Saving of trained models which can then be used in further experiments.
* Administrator functions, such as supervision of all scheduled experiments.




## Life-cycle

The project's GitHub repository can be found [here](https://github.com/krajj7/pikater).

### Installation

First and foremost, note several things:

1. The repository has several branches. Two of them are important:
	* `master` - contains the latest core system version.
	* `Eclipse-Vaadin-project` - contains the latest GUI (web application) and documentation versions.
2. This project requires a JPA-compatible database. At this moment, only PostGRE database is supported.

#### Database

1. Install a database of choice.
(<font color="red">TODO: něco přidat/upravit Petře?</font>)
2. When you clone the project on your machines in later steps, change the following files accordingly to your fresh database install:
	* `src/beans.xml`
	* `src/META-INF/persistence.xml` (<font color="red">TODO</font>)

#### Core system

Clone the `master` branch on target machine.

#### Extension

Import the project in Eclipse:

1. Install [Eclipse](https://www.eclipse.org/downloads/).
2. Install the [Vaadin plugin for Eclipse](http://vaadin.com/eclipse).
3. Clone the `Eclipse-Vaadin-project` branch on target machine.
4. Create a new Vaadin project, as specified [here](https://vaadin.com/book/-/page/getting-started.first-project.html). **You must use Vaadin version `7.1.14`**.
5. Overwrite the created project with the cloned pikater branch.
6. Check your project's Java binding - it might have been corrupted in the last step. Check the `.classpath` file.
5. Compile the `org.pikater.web.vaadin.gui.PikaterWidgetset.gwt.xml` widgetset.
6. Inspect/edit the `WebContent/WEB-INF/web.xml` file. It is used to configure the web application. Detailed information can be found [[here|Web-documentation#conf]].
7. Install and prepare a servlet container (e.g. Apache Tomcat).

### Deployment & launching

#### Database

Make it publicly available to the machines you wish to run core system or extension on.

#### Core system

Deployment was already done in installation.  
To launch the core system, execute the `Launch-core.launch` configuration.

#### Extension

The extension requires an instance of core system running locally so install and launch it first.

To deploy and launch the extension:

1. Start the servlet container.
2. Export the extension into a `.war` file from Eclipse, deploy it into the servlet container.
3. Start the deployed application.

<font color="red">TODO: tady (nebo nahoru) by měl Petr doplnit vygenerování iniciálního stavu DB</font>

4. Access the page, e.g. go to `http://localhost:8080/Pikater` (for Apache Tomcat; by default) or `http://my.domain/Pikater`. A login dialog will appear.
5. Enter the default login and password (see above) and start using. User guide can be found [[here|User-guide]].

### Maintenance

If extension is used, one or more administrators may need to manage it using the GUI. Otherwise, the application should be self-maintained.




## Wiki

This project contains inline documentation written in markdown, easily available as a wiki.

### Installation

1. Clone the `Eclipse-Vaadin-project` branch.
2. [[How to install|docs/guide_admin/wiki/01-Installation]]

### Launching

[[How to launch|docs/guide_admin/wiki/02-Launching]]

### Usage

[[How to use|docs/guide_admin/wiki/03-Usage]]




## Documentation<a name="docs"/>

[[Open documentation overview|Overview]]
