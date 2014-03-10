# MSF Account Management Tool
The MSF Account Management Tool is a tool to manage user accounts in the [MIRROR Spaces Framework][1]. The service is realized as plug-in for the [Openfire XMPP server][2].

## Build
An documentation how to setup an development environment for Openfire is described [here][3]. A developer guide for Openfire plugins including information how to build it is available [here][4].

## Installation & Update
The MSF Account Management Tool has been tested on Openfire 3.7 and higher. To install the plugin perform the following steps:

1. Open the administration console of Openfire and click on the "Plugins" tab.
2. In the "Upload Plugin" section of the page, select the persistenceService.jar file and submit it by pressing on the "Upload Plugin" button.

## Configuration
The configuration is integratd in the administration interface of Openfire: "Server" > "Server Settings" > "Account Management"

To enable the reset of user account passwords, two prerequisites have to be fulfilled:

* A sender e-mail address has to be configured in the plugin settings.
* A SMTP server must be configured within Openfire. The configuration can also be found in the administration console: "Server" > "Server Manager" > "Email Settings"

## Usage
The plugin provides a user frontend to perform account management actions. It is registered with the sub-path `/manage` of the plugin URL, for example `https://mirror-demo:9091/plugins/msfam/manage`.

## License
The MIRROR Spaces Service is released under the [Apache License 2.0][5].

## Changelog
v1.1.0-SNAPSHOT

<<<<<<< HEAD
* The password reset message can now be edited.

v1.0.1 -- March 10, 2014

=======
v1.0.1 -- March 10, 2014
>>>>>>> c7ff2bb7d094cda795fa68c2f37f715e01687711
* Changed tool name.

v1.0.0 -- February 28, 2014

* Initial release.


  [1]: https://github.com/MirrorIP/msf
  [2]: http://www.igniterealtime.org/projects/openfire/
  [3]: http://community.igniterealtime.org/docs/DOC-1020
  [4]: http://www.igniterealtime.org/builds/openfire/docs/latest/documentation/plugin-dev-guide.html
  [5]: http://www.apache.org/licenses/LICENSE-2.0.html
