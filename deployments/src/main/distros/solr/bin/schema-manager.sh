#! /bin/bash

##=======================================================================================
# Manages the schemas for solr cloud
#
# In this script schemas are managed via zookeeper. This approach was chosen because
# zookeeper is the central resource management tool for solr cloud. So it makes sense to
# keep the schemas house in zookeeper
##=======================================================================================

set -eo pipefail
IFS=$'\n\t '

readonly PERMITTED_USER=("uni_qgo")
readonly REPLACEMENT_SYMBOL="_replace_"
readonly REPO_SCHEMA_DIR="/nfs/public/rw/goa/.solr/repos/QuickGOBE/solr-cores/src/main/cores/${REPLACEMENT_SYMBOL}/conf"
readonly ACTION="$1"
readonly SCHEMA="$2"
readonly PROFILE="$3"

source "./common/common"

# ======= read the variables used by the control scripts ================================
source "../zookeeper.variables" || {
    echo "Please create a file called, zookeeper.variables, containing the necessary environment variables to setup zookeeper."
    exit 1
}

source "../solr.variables" || {
    echo "Please create a file called, solr.variables, containing the necessary environment variables to setup solr cloud."
    exit 1
}

# ========================= utility functions ===========================================
function show_help {
    cat<<EOF
  Usage: action schema profile

  Argument description:
    action       => The action to perform on zookeeper [update]
    schema       => Name of the schema to perform an action on [annotation|geneproduct|ontology]
    profile      => Indicates the profile of the zookeeper instances to execute the action on [dev|test|prod]

  Example: update annotation dev -- updates the annotation schema on the dev zookeeper instances

  WARNING: Please be aware that executing actions will have side effects on the solr cloud instances.
EOF
}

function schema_location() {
   local schema="$1"

   echo ${REPO_SCHEMA_DIR/_replace_/$schema}
}

# ====== check that the script has the right number of argument ========================
if [ "$#" -ne 3 ]; then
   show_help
   exit 1
fi

# ====== check which environment will be started ========================================
if ! check_profile VALID_PROFILES $PROFILE; then
   echo "Input profile: '$PROFILE', not recognized. Allowable values: $(print_valid_profiles VALID_PROFILES)"
   exit 1
fi

# ======= check the right user runs this script =========================================
if ! user_can_execute_script $USER PERMITTED_USER; then
    echo "This service can only be run as user(s), '${PERMITTED_USER[@]}'"
    exit 1
fi

case $ACTION in
  update)
     schema_dir=$(schema_location $SCHEMA)

     if [ ! -d "$schema_dir" ]; then
        echo "Schema: '$SCHEMA' does not exist in: ${REPO_SCHEMA_DIR%$REPLACEMENT_SYMBOL}. Make sure schema has been commited th VCS system"
        exit 1;
     fi

     $SOLR_LOCATION/server/scripts/cloud-scripts/zkcli.sh -zkhost $(zookeeper_hosts ${ZOO_HOSTS[$PROFILE]} $ZOO_PORT) -cmd upconfig -confname $SCHEMA -confdir $schema_dir
     ;;
  *)

  echo "Unrecognized action: $ACTION"
  exit 1
esac
