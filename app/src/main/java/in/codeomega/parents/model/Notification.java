package in.codeomega.parents.model;


import static in.codeomega.parents.interfaces.AppConstants.WEB_DOMAIN;

public class Notification {

    public String message,message_type,name,pic,voice_text,date,class_names,section_names;

    public Notification(String message, String message_type, String name, String pic, String voice_text, String date, String class_names, String section_names) {

        if (voice_text.contains("voc")) {

            this.message = message.replace("../",WEB_DOMAIN);

        }else if (voice_text.contains("txt")) {

            this.message = message;
        }


        this.message_type = message_type;
        this.name = name;
        this.pic = pic;
        this.voice_text = voice_text;
        this.date = date;
        this.class_names = class_names;
        this.section_names = section_names;

    }

}
