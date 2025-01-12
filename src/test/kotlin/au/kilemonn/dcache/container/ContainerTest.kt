package au.kilemonn.dcache.container

import org.testcontainers.containers.GenericContainer

/**
 * A base class to give specific container interaction methods.
 *
 * @author github.com/Kilemonn
 */
abstract class ContainerTest
{
    protected fun isPaused(container: GenericContainer<*>): Boolean
    {
        if (container.isRunning)
        {
            container.dockerClient.inspectContainerCmd(container.containerId).use {
                val result = it.exec()
                return result.state.paused == true
            }
        }
        return false
    }

    protected fun pause(container: GenericContainer<*>)
    {
        if (container.isRunning && !isPaused(container))
        {
            container.dockerClient.pauseContainerCmd(container.containerId).use {
                it.exec()
            }
        }
    }

    protected fun unpause(container: GenericContainer<*>)
    {
        if (container.isRunning && isPaused(container))
        {
            container.dockerClient.unpauseContainerCmd(container.containerId).use {
                it.exec()
            }
        }
    }

    protected fun getEndpoint(container: GenericContainer<*>, port: Int): String
    {
        return "${container.host}:${container.getMappedPort(port)}"
    }

    /**
     * [pause] the provided [container] and calls the [function] then [unpause] the container.
     */
    protected fun whilePaused(container: GenericContainer<*>, function: Runnable)
    {
        try
        {
            pause(container)
            function.run()
        }
        finally
        {
            unpause(container)
        }
    }
}
