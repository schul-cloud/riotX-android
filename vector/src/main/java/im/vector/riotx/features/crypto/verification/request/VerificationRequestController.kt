/*
 * Copyright 2019 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package im.vector.riotx.features.crypto.verification.request

import androidx.core.text.toSpannable
import com.airbnb.epoxy.EpoxyController
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.Uninitialized
import im.vector.riotx.R
import im.vector.riotx.core.epoxy.dividerItem
import im.vector.riotx.core.resources.ColorProvider
import im.vector.riotx.core.resources.StringProvider
import im.vector.riotx.core.utils.colorizeMatchingText
import im.vector.riotx.features.crypto.verification.VerificationBottomSheetViewState
import im.vector.riotx.features.crypto.verification.epoxy.bottomSheetVerificationActionItem
import im.vector.riotx.features.crypto.verification.epoxy.bottomSheetVerificationNoticeItem
import im.vector.riotx.features.crypto.verification.epoxy.bottomSheetVerificationWaitingItem
import javax.inject.Inject

class VerificationRequestController @Inject constructor(
        private val stringProvider: StringProvider,
        private val colorProvider: ColorProvider
) : EpoxyController() {

    var listener: Listener? = null

    private var viewState: VerificationBottomSheetViewState? = null

    fun update(viewState: VerificationBottomSheetViewState) {
        this.viewState = viewState
        requestModelBuild()
    }

    override fun buildModels() {
        val state = viewState ?: return
        val matrixItem = viewState?.otherUserMxItem ?: return

        if (state.selfVerificationMode) {
            bottomSheetVerificationNoticeItem {
                id("notice")
                notice(stringProvider.getString(R.string.verification_open_other_to_verify))
            }

            dividerItem {
                id("sep")
            }

            bottomSheetVerificationWaitingItem {
                id("waiting")
                title(stringProvider.getString(R.string.verification_request_waiting, matrixItem.getBestName()))
            }

            bottomSheetVerificationActionItem {
                id("passphrase")
                title(stringProvider.getString(R.string.verification_cannot_access_other_session))
                titleColor(colorProvider.getColorFromAttribute(R.attr.riotx_text_primary))
                subTitle(stringProvider.getString(R.string.verification_use_passphrase))
                iconRes(R.drawable.ic_arrow_right)
                iconColor(colorProvider.getColorFromAttribute(R.attr.riotx_text_primary))
                listener { listener?.onClickRecoverFromPassphrase() }
            }
            bottomSheetVerificationActionItem {
                id("skip")
                title(stringProvider.getString(R.string.skip))
                titleColor(colorProvider.getColor(R.color.riotx_destructive_accent))
//                subTitle(stringProvider.getString(R.string.verification_use_passphrase))
                iconRes(R.drawable.ic_arrow_right)
                iconColor(colorProvider.getColor(R.color.riotx_destructive_accent))
                listener { listener?.onClickDismiss() }
            }
        } else {
            val styledText = matrixItem.let {
                stringProvider.getString(R.string.verification_request_notice, it.id)
                        .toSpannable()
                        .colorizeMatchingText(it.id, colorProvider.getColorFromAttribute(R.attr.vctr_notice_text_color))
            }

            bottomSheetVerificationNoticeItem {
                id("notice")
                notice(styledText)
            }

            dividerItem {
                id("sep")
            }

            when (val pr = state.pendingRequest) {
                is Uninitialized -> {
                    bottomSheetVerificationActionItem {
                        id("start")
                        title(stringProvider.getString(R.string.start_verification))
                        titleColor(colorProvider.getColor(R.color.riotx_accent))
                        subTitle(stringProvider.getString(R.string.verification_request_start_notice))
                        iconRes(R.drawable.ic_arrow_right)
                        iconColor(colorProvider.getColorFromAttribute(R.attr.riotx_text_primary))
                        listener { listener?.onClickOnVerificationStart() }
                    }
                }
                is Loading       -> {
                    bottomSheetVerificationWaitingItem {
                        id("waiting")
                        title(stringProvider.getString(R.string.verification_request_waiting_for, matrixItem.getBestName()))
                    }
                }
                is Success       -> {
                    if (!pr.invoke().isReady) {
                        bottomSheetVerificationWaitingItem {
                            id("waiting")
                            title(stringProvider.getString(R.string.verification_request_waiting_for, matrixItem.getBestName()))
                        }
                    }
                }
            }
        }
    }

    interface Listener {
        fun onClickOnVerificationStart()
        fun onClickRecoverFromPassphrase()
        fun onClickDismiss()
    }
}
