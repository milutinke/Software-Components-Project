# Software Components course Project

This project was written for a Software Components course project on my university in Java.
The project requirement was to create a database that could have multiple different implementations and a test program that would show that off.
So, we needed to create a clean API that specific implementations packaged into jars could utilize to achieve the goal.

# dist

This folder contains binaries.

# source

This folder includes the sources of:
- `spec` - Specification (API)
- `tsapp` - The test program
- `impl.json` - Concrete implementation that is using JSON as a way to serialize data
- `impl.json` - Concrete implementation that is using YAML as a way to serialize data

PS: There was third (optional) requirement to implement an a flat file with indexing, but due to having COVID-19 I was not able to code this part.

PS: The project was done in 2020.