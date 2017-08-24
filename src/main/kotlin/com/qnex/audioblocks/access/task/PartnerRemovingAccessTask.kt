package com.qnex.audioblocks.access.task

import com.qnex.audioblocks.access.model.PartnerInfo

class PartnerRemovingAccessTask(val taskName: String, accessTaskContext: PartnerAccessTaskContext) : ActionTask<PartnerInfo> {
    override fun start() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun stop() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun pause() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun continuee() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    override fun getName(): String {
        return taskName
    }
}