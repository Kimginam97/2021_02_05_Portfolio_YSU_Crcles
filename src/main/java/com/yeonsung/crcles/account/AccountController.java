package com.yeonsung.crcles.account;

import com.yeonsung.crcles.account.form.SignUpForm;
import com.yeonsung.crcles.account.validator.SignUpFormValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final SignUpFormValidator signUpFormValidator;
    private final AccountRepository accountRepository;

    // 커스텀한 signUpForm 검증 (이메일,닉네임 중복검사)
    @InitBinder("signUpForm")
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(signUpFormValidator);
    }

    // 회원가입
    @GetMapping("/sign-up")
    public String signUpFormView(Model model){
        model.addAttribute("signUpForm",new SignUpForm());
        return "account/sign-up";
    }

    @PostMapping("/sign-up")
    public String signUpFormProcess(@Valid SignUpForm signUpForm, Errors errors){
        if(errors.hasErrors()){
            return "account/sign-up";
        }

        Account account = accountService.processNewAccount(signUpForm);
        accountService.login(account);
        return "redirect:/";
    }

    // 이메일 체크 토큰
    @GetMapping("/check-email-token")
    public String checkEmailToken(String token,String email,Model model){

        Account account = accountRepository.findByEmail(email);
        String view = "account/checked-email";

        if (account == null){
            model.addAttribute("error","wrong.email");
            return view;
        }

        if(!account.isValidToken(token)){
            model.addAttribute("error","wrong.email");
            return view;
        }

        accountService.completeSignUp(account);
        model.addAttribute("nickname",account.getNickname());
        return view;
    }

    // 이메일 확인기능
    @GetMapping("/check-email")
    public String checkEmail(@CurrentAccount Account account, Model model) {
        model.addAttribute("email", account.getEmail());
        return "account/check-email";
    }

    // 이메일 재전송 기능
    @GetMapping("/resend-confirm-email")
    public String resendConfirmEmail(@CurrentAccount Account account, Model model) {
        if (!account.isSendConfirmEmail()) {
            model.addAttribute("error", "인증 이메일은 1시간에 한번만 전송할 수 있습니다.");
            model.addAttribute("email", account.getEmail());
            return "account/check-email";
        }

        accountService.sendSignUpConfirmEmail(account);
        return "redirect:/";
    }

    // 회원 프로필
    @GetMapping("/profile/{nickname}")
    public String viewProfile(@PathVariable String nickname, Model model, @CurrentAccount Account account) {

        // 닉네임을 통해서 회원 정보를 찾아 온다
        Account byNickname = accountRepository.findByNickname(nickname);

        // 닉네임이 없을 경우
       if (nickname == null) {
            throw new IllegalArgumentException(nickname + "에 해당하는 사용자가 없습니다.");
        }

        model.addAttribute("account",byNickname);
        model.addAttribute("isOwner",byNickname.equals(account));
        return "account/profile";
    }


}
