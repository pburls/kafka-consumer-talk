#!/usr/bin/env bash

script_dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
project_dir="${script_dir}/../"

cd $project_dir

# run the e2e integration tests
../gradlew :integration:test

# copy the test results to the correct location for the jenkins publisher
# https://github.com/sky-uk/jenkins-tooling/blob/36d28ee4dae27a10d3fde208cb9b73b38136dbb9/examples/reference/cd.yml#L61
cp -R build/test-results/test/ build/test-results/junit-reports/
