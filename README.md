# EasyTravel

This project combines many technologies in order to simulate a commercial travel angency website alongside with generating traffic and creating some problems.

## Building the project

In order to build the project locally go to the `Distribution` directory and run the ant command: `ant -f build.xml all`. This can take up to 10 minutes. The build result can be found in the `dist` subdirectory.

## Angular frontend

By default the angular frontend used is taken from the directory `easytravel-angular\last-build` that is part of the sources. If you need to change it, then:

1. Check if you already have the sources - there should be a directory `easytravel-angular` with the sources. If you have them, then go to point 3.
2. Clone the repository SECRET - it is best to clone it inside the directory with easyTravel sources.
3. Make the changes you need. Build the angular project by running `yarn build` inside the project.
4. Copy the changed files to the dist with the script `ant -f build.xml copyAngular` (run from within `easyTravel/Distribution`). Do remember, that the files copied are supposed to be in the directory `easytravel-angular\last-build`. If they are not here but in another place, please update the `build.xml` file accordingly.

