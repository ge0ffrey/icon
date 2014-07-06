#!/bin/sh

curl -s http://iconchallenge.insight-centre.org/sites/default/files/leaderboard.html | grep "OptaPlanner Delirium" | sed -e 's/.*OptaPlanner Delirium/Opta/g' | sed -e 's/<[^>]*>/    /g' | nl -v -1
