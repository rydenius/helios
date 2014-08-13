Helios Standalone
=================

This is a docker image containing the helios master, agent and skydns along
with a few scripts for managing it.

The idea is that getting a fully functional helios environment running locally
should be straightforward so people can play around with it and easily run
integration tests locally.


Prerequisites
-------------

* Docker installed and running.

  https://docs.docker.com/installation/mac/

  https://docs.docker.com/installation/ubuntulinux/

* Helios cli installed: `brew install helios` or `apt-get install helios`


Quickstart
----------

1. `brew install helios-standalone` or `apt-get install helios-standalone`
2. `helios-up`
3. profit!


Example
-------

```sh
# Start helios-standalone
helios-up

# Create a trivial helios job
helios-standalone create test:1 busybox -- sh -c "while :; do sleep 1; done"

# Deploy the job on the (local) standalone host
helios-standalone deploy test:1 standalone

# Check the job status
helios-standalone status

# Undeploy job
helios-standalone undeploy -a -f test:1

# Remove job
helios-standalone remove test:1

# Stop helios-standalone
helios-down
```


Commands
--------

* `helios-up`

  Brings up the helios-standalone container.

* `helios-down`

  Destroys the helios-standalone container.

* `helios-standalone ...`

  Wrapper around the helios cli for talking to helios-standalone.

  Essentially identical to `eval $(helios-env) && helios -z $HELIOS_URI ...`.

* `helios-restart`

  Destroy and restart the helios-standalone container.

* `helios-cleanup`

  Remove all helios-standalone jobs and containers.

* `helios-env`

  Utility for setting `HELIOS_URI` environment variable in your
  shell: `eval $(helios-env)`.


Debugging
---------

There's a `helios-enter` command that can be used to bring up the
helios-standalone container and get a bash shell inside it. There's
currently no way to get inside a running helios-standalone.


TODO
----

* Add a helios-status command for checking if helios-standalone is running/healthy.
