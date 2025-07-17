package com.iobits.budgetexpensemanager.ui.fragments

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.net.Uri
import android.os.Bundle
import android.text.TextPaint
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.BounceInterpolator
import android.webkit.URLUtil
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.iobits.budgetexpensemanager.R
import com.iobits.budgetexpensemanager.databinding.FragmentPremiumBinding
import com.iobits.budgetexpensemanager.managers.BillingManagerV5
import com.iobits.budgetexpensemanager.myApplication.MyApplication
import com.iobits.budgetexpensemanager.ui.viewModels.DataShareViewModel
import com.iobits.budgetexpensemanager.utils.AdsCounter
import com.iobits.budgetexpensemanager.utils.Constants
import com.iobits.budgetexpensemanager.utils.changeStatusBarColor
import com.iobits.budgetexpensemanager.utils.handleBackPress
import com.iobits.budgetexpensemanager.utils.invisible
import com.iobits.budgetexpensemanager.utils.popBackStack
import com.iobits.budgetexpensemanager.utils.safeNavigate
import com.iobits.budgetexpensemanager.utils.visible
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class PremiumFragment : Fragment() {

    val binding by lazy {
        FragmentPremiumBinding.inflate(layoutInflater)
    }
    private val shareViewModel: DataShareViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        shareViewModel.addTopSpacing?.invoke(true)
        if (MyApplication.sessionStart) {
            MyApplication.mInstance.adsManager.loadInterstitialAdAutoSplash(
                requireActivity(),
                adLoaded = {},
                failedToLoadAd = {})
        }
        AdsCounter.showPro = 0
        initViews()
        setAnimation()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        MyApplication.mInstance.isOutForRating =  true
        handleBackPress {
            MyApplication.mInstance.isOutForRating =  false
//            popBackStack()
            safeNavigate(R.id.action_premiumFragment_to_dashboardFragment, R.id.premiumFragment)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        MyApplication.mInstance.isOutForRating =  false
        shareViewModel.addTopSpacing?.invoke(false)
    }

    private fun setAnimation() {
        val shake: Animation = AnimationUtils.loadAnimation(requireContext(), R.anim.shake)
        lifecycleScope.launch(Dispatchers.IO) {
            while (true) {
                withContext(Dispatchers.Main) {
                    binding.startBtn.startAnimation(shake) // starts animation
                }
                delay(2000)
            }
        }
    }

    private fun initViews() {
        val paint: TextPaint = binding.mainText.paint
        val width: Float = paint.measureText(binding.mainText.text.toString())
        val textShader: Shader = LinearGradient(
            0f, 0f, width, binding.mainText.textSize, intArrayOf(
                Color.parseColor("#FF48E0"),
                Color.parseColor("#3363F2")
            ), null, Shader.TileMode.CLAMP
        )
        val initialBackgroundY = binding.discountImg.y
        val backgroundDrop = ObjectAnimator.ofFloat(
            binding.discountImg,
            View.TRANSLATION_Y,
            -2000f,
            initialBackgroundY - binding.discountImg.top
        ).apply {
            duration = 1400
            interpolator = BounceInterpolator()
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {
                    binding.discountImg.visibility = View.VISIBLE
                }

                override fun onAnimationEnd(animation: Animator) {}
                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
            })
        }
        val animatorSet = AnimatorSet().apply {
            playSequentially(backgroundDrop)
        }
        binding.apply {
            animatorSet.start()
            startBtn.setOnClickListener {
                MyApplication.mInstance.billingManagerV5.subscription(requireActivity())
            }
            mainText.paint.shader = textShader
            backBtn.setOnClickListener {
                MyApplication.mInstance.isOutForRating =  false
                if (MyApplication.sessionStart) {
                    changeStatusBarColor(requireActivity(), R.color.white)
                    MyApplication.mInstance.adsManager.showInterstitialAd(requireActivity()) {
                        MyApplication.sessionStart = false
                        shareViewModel.addTopSpacing?.invoke(false)
//                        popBackStack()
                        safeNavigate(
                            R.id.action_premiumFragment_to_dashboardFragment,
                            R.id.premiumFragment
                        )
                    }
                } else {

                    //popBackStack()
                    safeNavigate(
                        R.id.action_premiumFragment_to_dashboardFragment,
                        R.id.premiumFragment
                    )
                }
            }

            setPrice()
        }

        binding.privacyPolicy.setOnClickListener {
            showPrivacyPolicy(requireContext())
        }
        binding.restore.setOnClickListener {
            showCancelSub()
        }
        binding.terms.setOnClickListener {
            showTerms(requireContext())
        }

    }

    private fun showCancelSub() {
        if (URLUtil.isValidUrl(getString(R.string.cancel_subscription_url))) {
            val i = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(getString(R.string.cancel_subscription_url))
            )
            this.startActivity(i)
        }
    }

    private fun showPrivacyPolicy(context: Context) {
        if (URLUtil.isValidUrl("https://igniteapps.blogspot.com/2024/03/privacy-policy-expense-tracker.html")) {
            val i = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://igniteapps.blogspot.com/2024/03/privacy-policy-expense-tracker.html")
            )
            context.startActivity(i)
        }
    }

    private fun showTerms(context: Context) {
        if (URLUtil.isValidUrl("https://igniteapps.blogspot.com/2024/10/terms-conditions-for-expense-manager.html")) {
            val i = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://igniteapps.blogspot.com/2024/10/terms-conditions-for-expense-manager.html")
            )
            context.startActivity(i)
        }
    }

    private fun setPrice() {
        try {
            if (BillingManagerV5.subPremiumPriceAfterDiscount == "" || BillingManagerV5.subPremiumPriceAfterDiscount.isEmpty()) {
                if (BillingManagerV5.subPremiumPrice.isEmpty()) {
                    binding.currentPrice.text = "$1.49"
                    binding.subTitle.text = "/week"
                    binding.priceMoreLayout.invisible()
                } else {
                    binding.currentPrice.text = "${BillingManagerV5.subPremiumPrice}"
                    binding.priceMoreLayout.invisible()
                }
            } else {
                binding.currentPrice.text = "${BillingManagerV5.subPremiumPrice}"
                binding.priceMore.text = "${BillingManagerV5.subPremiumPriceAfterDiscount}"
                binding.priceMoreLayout.visible()
            }
        } catch (e: Exception) {
            Log.d("PRO_ACTIVITY", "ERROR : ${e.localizedMessage}")
        }
    }

    private fun Int.dpToPx(): Int {
        val scale = Resources.getSystem().displayMetrics.density
        return (this * scale + 0.5f).toInt()
    }
}