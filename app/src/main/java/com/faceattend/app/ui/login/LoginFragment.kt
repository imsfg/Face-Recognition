package com.faceattend.app.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.faceattend.app.R
import com.faceattend.app.databinding.FragmentLoginBinding
import com.faceattend.app.ui.appViewModels
import com.google.android.material.snackbar.Snackbar

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel by appViewModels { container ->
        LoginViewModel(container.staffRepository, container.sessionManager)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.signInButton.setOnClickListener {
            viewModel.signIn(
                binding.usernameInput.text?.toString().orEmpty(),
                binding.passwordInput.text?.toString().orEmpty()
            )
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is LoginState.AdminSignedIn -> {
                    findNavController().navigate(R.id.action_login_to_staffList)
                    viewModel.onStateHandled()
                }

                is LoginState.StaffSignedIn -> {
                    findNavController().navigate(R.id.action_login_to_staffHome)
                    viewModel.onStateHandled()
                }

                is LoginState.Error -> {
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                    viewModel.onStateHandled()
                }

                LoginState.Idle -> Unit
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
