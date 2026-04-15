package com.garmin.fit

abstract class MesgFilterBase : MesgSource, MesgListener {
    private val mesgListeners = ArrayList<MesgListener>()

    override fun onMesg(mesg: Mesg?) {
        for (mesgListener in mesgListeners) {
            mesgListener.onMesg(mesg)
        }
    }

    override fun addListener(mesgListener: MesgListener?) {
        if ((mesgListener != null) && !mesgListeners.contains(mesgListener)) {
            mesgListeners.add(mesgListener)
        }
    }

    fun removeListener(mesgListener: MesgListener?) {
        if ((mesgListener != null)) {
            mesgListeners.remove(mesgListener)
        }
    }
}
