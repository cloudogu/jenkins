---
title: "Post-initialization scripts"
---

# Post-initialization scripts

At the end of the Jenkins startup process, all scripts located inside /var/tmp/resources/init.groovy.d are executed. You
can add your own scripts by saving them inside the `custom.init.groovy.d` volume.

## System critical scripts

To mark a script as system **critical** add the **prefix** **CRIT** after the script number. A critical script can be
defined if an error leads to a jenkins instance that e.g. can not be reached or can not be logged in to.

**Example:** `init030CRITinstallplugins.groovy`

## Non-critical scripts (Normal)

To mark a script as **non-critical** add the **prefix** **NORM** after the script number. Modifications which only apply
to additional features of Jenkins are usually non-critical (NORM) e.g. configuring a JDK or Maven.

If scripts are marked as neither **CRIT** nor **NORM** they will be treated as non-critical.

**Example:** `init100NORMmavenautoinstall.groovy`
