#!/usr/bin/env bash

script_dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
project_dir="${script_dir}/../"

cd $project_dir

# run the e2e integration tests
../gradlew :integration:testWip :integration:test
