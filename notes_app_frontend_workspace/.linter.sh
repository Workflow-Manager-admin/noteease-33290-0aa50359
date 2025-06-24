#!/bin/bash
cd /home/kavia/workspace/code-generation/noteease-33290-0aa50359/notes_app_frontend_workspace/notes_app_frontend
./gradlew lint
LINT_EXIT_CODE=$?
if [ $LINT_EXIT_CODE -ne 0 ]; then
   exit 1
fi

