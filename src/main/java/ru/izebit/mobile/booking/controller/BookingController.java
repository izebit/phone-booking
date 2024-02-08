package ru.izebit.mobile.booking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.izebit.mobile.booking.dto.Message;
import ru.izebit.mobile.booking.dto.Phone;
import ru.izebit.mobile.booking.dto.Reservation;
import ru.izebit.mobile.booking.service.PhoneInformationService;
import ru.izebit.mobile.booking.service.PhoneService;
import ru.izebit.mobile.booking.service.UserService;
import ru.izebit.mobile.booking.service.exception.*;

import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/booking/")
@RequiredArgsConstructor
public class BookingController {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm 'on' d MMMM uu");
    private final PhoneService phoneService;
    private final PhoneInformationService phoneInformationService;
    private final UserService userService;

    @RequestMapping(value = "login")
    public String loginPage() {
        return "login.html";
    }

    @GetMapping(value = "signup")
    public String signUpPage() {
        return "signup.html";
    }

    @PostMapping(value = "signup")
    public String signUp(@ModelAttribute("username") final String username,
                         @ModelAttribute("password") final String password,
                         final Model model) {
        try {
            userService.createUser(username, password);
            model.addAttribute("message", new Message(Message.Type.INFO,
                    "User with username: %s and password: %s has been created. Don't forget your credentials".formatted(username, password)));
        } catch (EmptyUsernameException e) {
            model.addAttribute("message", new Message(Message.Type.WARNING, "Username must be not blank."));
        } catch (EmptyPasswordException e) {
            model.addAttribute("message", new Message(Message.Type.WARNING, "Password must be not blank."));
        } catch (UsernameIsAlreadyUsedException e) {
            model.addAttribute("message", new Message(Message.Type.WARNING, "User with the name already exists. Try to choose another one."));
        }
        return "signup.html";
    }

    private void fillPhonesModel(final Model model, final User user) {
        var phones = phoneService.getAllPhonesWithReservations()
                .parallelStream()
                .map(pair -> {
                    var phone = pair.getKey();
                    var reservation = pair.getValue();
                    var information = phoneInformationService.findFor(phone.getTitle());
                    if (reservation == null)
                        return Phone.builder()
                                .id(phone.getId())
                                .title(phone.getTitle())
                                .details(information)
                                .build();
                    else
                        return Phone.builder()
                                .id(phone.getId())
                                .title(phone.getTitle())
                                .reservation(
                                        Reservation.builder()
                                                .username(reservation.getUsername())
                                                .date(reservation.getBookedDate().format(DATE_TIME_FORMATTER))
                                                .doesUserOwn(user.getUsername().equals(reservation.getUsername()))
                                                .build())
                                .details(information)
                                .build();
                })
                .toList();

        model.addAttribute("phones", phones);
        model.addAttribute("username", user.getUsername());
    }

    @RequestMapping(value = "phones")
    public String phonesPage(final Model model,
                             @AuthenticationPrincipal final User user) {
        fillPhonesModel(model, user);
        return "phones.html";
    }

    @RequestMapping(value = "phones/{id}/book")
    public String book(final RedirectAttributes redirectAttributes,
                       @PathVariable("id") final Long phoneId,
                       @AuthenticationPrincipal final User user) {
        try {
            phoneService.bookPhoneBy(phoneId, user.getUsername());
            var phoneTitle = phoneService.getPhoneById(phoneId)
                    .map(ru.izebit.mobile.booking.model.Phone::getTitle)
                    .orElseThrow(PhoneNotFoundException::new);

            redirectAttributes.addFlashAttribute("message", new Message(Message.Type.INFO, "You successfully booked " + phoneTitle + ". Congratulations \uD83D\uDE4C"));
        } catch (PhoneNotFoundException e) {
            redirectAttributes.addFlashAttribute("message", new Message(Message.Type.ERROR, "Can't find the required phone."));
        } catch (PhoneIsAlreadyBookedException e) {
            redirectAttributes.addFlashAttribute("message", new Message(Message.Type.WARNING, "You are trying to take the phone that already booked."));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", new Message(Message.Type.ERROR, "There is an unexpected error, try a bit later."));
        }
        return "redirect:/booking/phones";
    }

    @RequestMapping(value = "phones/{id}/release")
    public String release(final RedirectAttributes attributes,
                          @PathVariable("id") final Long phoneId,
                          @AuthenticationPrincipal final User user) {
        try {
            phoneService.releasePhoneBy(phoneId, user.getUsername());
            var phoneTitle = phoneService.getPhoneById(phoneId)
                    .map(ru.izebit.mobile.booking.model.Phone::getTitle)
                    .orElseThrow(PhoneNotFoundException::new);

            attributes.addFlashAttribute("message", new Message(Message.Type.INFO, "You successfully returned " + phoneTitle + ". Congratulations \uD83D\uDE4C"));
        } catch (PhoneNotFoundException e) {
            attributes.addFlashAttribute("message", new Message(Message.Type.ERROR, "Can't find the required phone."));
        } catch (PhoneIsBookedBySomebodyElseException e) {
            attributes.addFlashAttribute("message", new Message(Message.Type.WARNING, "You are trying to return the phone that you didn't book."));
        } catch (PhoneIsNotBookedException e) {
            attributes.addFlashAttribute("message", new Message(Message.Type.WARNING, "You can't return the phone that is not booked."));
        } catch (Exception e) {
            attributes.addFlashAttribute("message", new Message(Message.Type.ERROR, "There is an unexpected error, try a bit later."));
        }

        return "redirect:/booking/phones";
    }
}
