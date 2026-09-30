//
//  ViewController.swift
//  IDOEditScreenDemo
//
//  Created by cyf on 2025/3/13.
//

import UIKit
import protocol_channel
import SVProgressHUD

fileprivate let sportCellIdentifier = "SportCellIdentifier"

class SportListVC: UITableViewController {
    
    var screenInfoReplyModel:IDOSportScreenInfoReplyModel?

    override func viewDidLoad() {
        super.viewDidLoad()
        self.navigationController?.title = "Sport type list"
        self.view.backgroundColor = .white
        self.tableView.backgroundColor = .white
        // Do any additional setup after loading the view.
        self.tableView.register(UITableViewCell.self, forCellReuseIdentifier: sportCellIdentifier)
        
        getSportScreenBaseInfoCmd()
    }
    
    func getSportScreenBaseInfoCmd(){
        SVProgressHUD.show()
        // Get basic info for the in-sport screen display
        Cmds.getSportScreenBaseInfo().send { res in
            SVProgressHUD.dismiss()
            if case .success(let o) = res {
                // Succeeded
                self.screenInfoReplyModel = o
                print("get sport screen base info success: \(String(describing: o?.toJsonString()))")
                self.tableView.reloadData()
            }else
            {
                // Failed
                print("get sport screen base info failure")
            }
        }
    }
    
    func getSportName(_ sportType: Int) -> String{
        switch sportType {
        case 0x01: // Walk
            return "walk".i18n
        case 0x02 : //Run
            return "run".i18n
        case 0x03 : //Ride
            return "ride".i18n
        case 0x04 : //Hike
            return "hike".i18n
        case 0x05 : //Swim
            return "swim".i18n
        case 0x06 : //Mountain climbing
            return "mountain climbing".i18n
        case 0x07 : //Badminton
            return "badminton".i18n
        case 0x08 : //Other
            return "other".i18n
        case 0x09 : //Fitness
            return "fitness".i18n
        case 0x0A : //Spinning
            return "spinning".i18n
        case 0x0B : //Elliptical machine
            return "elliptical machine".i18n
        case 0x0C : //Treadmill
            return "treadmill".i18n
        case 0x0D : //Sit-ups
            return "sit-ups".i18n
        case 0x0E : //Push-ups
            return "push-ups".i18n
        case 0x0F : //Dumbbells
            return "dumbbells".i18n
        case 0x10 : //Weight lifting
            return "weight lifting".i18n
        case 0x11 : //Calisthenics
            return "calisthenics".i18n
        case 0x12 : //Yoga
            return "yoga".i18n
        case 0x13 : //Rope skipping
            return "rope skipping".i18n
        case 0x14 : //Table tennis
            return "table tennis".i18n
        case 0x15 : //Basketball
            return "basketball".i18n
        case 0x16 : //Football
            return "football".i18n
        case 0x17 : //Volleyball
            return "volleyball".i18n
        case 0x18 : //Tennis
            return "tennis".i18n
        case 0x19 : //Golf
            return "golf".i18n
        case 0x1A : //Baseball
            return "baseball".i18n
        case 0x1B : //Skating
            return "skiing".i18n
        case 0x1C : //Roller skating
            return "roller skating".i18n
        case 0x1D : //Dancing
            return "dancing".i18n
        case 0x1F : //Roller trainer
            return "indoor rowing".i18n
        case 0x20 : //Pilates
            return "pilates".i18n
        case 0x21 : //Cross training
            return "cross training".i18n
        case 0x22 : //Aerobics
            return "aerobics".i18n
        case 0x23 : //Zumba
            return "zumba".i18n
        case 0x24 : //Square dance
            return "square dance".i18n
        case 0x25 : //Plank
            return "plank".i18n
        case 0x26 : //Gym
            return "gym".i18n
        case 0x27 : //Aerobic calisthenics
            return ""
        case 0x30 : //Outdoor running
            return "outdoor running".i18n
        case 0x31 : //Indoor running
            return "indoor running".i18n
        case 0x32 : //Outdoor cycling
            return "indoor cycling".i18n
        case 0x33 : //Indoor cycling
            return "indoor cycling".i18n
        case 0x34 : //Outdoor walking
            return "outdoor walking".i18n
        case 0x35 : //Indoor walking
            return "indoor walking".i18n
        case 0x36 : //Indoor swimming (pool swimming)
            return "pool swimming".i18n
        case 0x37 : //Outdoor swimming (open water swimming)
            return "open water swimming".i18n
        case 0x38 : //Elliptical machine
            return "elliptical machine".i18n
        case 0x39 : //Rowing machine
            return "rowing machine".i18n
        case 0x3A : //High-intensity interval training
            return "high-intensity interval training".i18n
        case 0x4B : //Cricket
            return "cricket".i18n
        case 0x64 : //Free training
            return "free training".i18n
        case 0x65 : //Functional strength training
            return "functional strength training".i18n
        case 0x66 : //Core training
            return "core training".i18n
        case 0x67 : //Stepper
            return "treadmills".i18n
        case 0x68 : //Cooldown
            return "organize and relax".i18n
        case 0x6E : //Traditional strength training
            return "traditional strength training".i18n
        case 0x70 : //Pull-ups
            return "pull ups".i18n
        case 0x72 : //Jumping jacks
            return "jumping jacks".i18n
        case 0x73 : //Squats
            return "squats".i18n
        case 0x74 : //High knees
            return "high knees".i18n
        case 0x75 : //Boxing
            return "boxing".i18n
        case 0x76 : //Barbell
            return "barbell".i18n
        case 0x77 : //Martial arts
            return "martial arts".i18n
        case 0x78 : //Tai chi
            return "tai chi".i18n
        case 0x79 : //Taekwondo
            return "taekwondo".i18n
        case 0x7A : //Karate
            return "karate".i18n
        case 0x7B : //Kickboxing
            return "kickboxing".i18n
        case 0x7C : //Fencing
            return "fencing".i18n
        case 0x7D : //Archery
            return "archery".i18n
        case 0x7E : //Gymnastics
            return "gymnastics".i18n
        case 0x7F : //Horizontal bar
            return "horizontal bar".i18n
        case 0x80 : //Parallel bars
            return "parallel bar".i18n
        case 0x81 : //Walking machine
            return "walking machine".i18n
        case 0x82 : //Climbing machine
            return "climbing machine".i18n
        case 0x83 : //Bowling
            return "bowling".i18n
        case 0x84 : //Billiards
            return "billiards".i18n
        case 0x85 : //Hockey
            return "hockey".i18n
        case 0x86 : //Rugby
            return "rugby".i18n
        case 0x87 : //Squash
            return "squash".i18n
        case 0x88 : //Softball
            return "softball".i18n
        case 0x89 : //Handball
            return "handball".i18n
        case 0x8A : //Shuttlecock
            return "shuttlecock".i18n
        case 0x8B : //Beach soccer
            return "beach soccer".i18n
        case 0x8C : //Sepak takraw
            return "sepak takraw".i18n
        case 0x8D : //Dodgeball
            return "dodgeball".i18n
        case 0x98 : //Hip-hop
            return "hip-hop".i18n
        case 0x99 : //Ballet
            return "ballet".i18n
        case 0x9A : //Social dance
            return "social dance".i18n
        case 0x9B : //Frisbee
            return "frisbee".i18n
        case 0x9C : //Darts
            return "darts".i18n
        case 0x9D : //Horseback riding
            return "horseback riding"
        case 0x9E : //Stair climbing
            return "stair climbing".i18n
        case 0x9F : //Kite flying
            return "kite flying".i18n
        case 0xA0 : //Fishing
            return "fishing".i18n
        case 0xA1 : //Sled
            return "sled".i18n
        case 0xA2 : //Bobsleigh
            return "snowmobile".i18n
        case 0xA3 : //Snowboarding
            return "snowboard".i18n
        case 0xA4 : //Snow sports
            return "snow sports".i18n
        case 0xA5 : //Alpine skiing
            return "alpine skiing".i18n
        case 0xA6 : //Cross-country skiing
            return "cross-country skiing".i18n
        case 0xA7 : //Curling
            return "curling".i18n
        case 0xA8 : //Ice hockey
            return "ice hockey".i18n
        case 0xA9 : //Biathlon
            return "biathlon".i18n
        case 0xAA : //Surfing
            return "surfing".i18n
        case 0xAB : //Sailing
            return "sailing".i18n
        case 0xAC : //Windsurfing
            return "windsurfing".i18n
        case 0xAD : //Kayak
            return "kayak".i18n
        case 0xAE : //Motorboat
            return "motorboat".i18n
        case 0xAF : //Paddling
            return "rowing".i18n
        case 0xB0 : //Rowing
            return "rowing".i18n
        case 0xB1 : //Dragon boat
            return "dragon boat".i18n
        case 0xB2 : //Water polo
            return "water polo".i18n
        case 0xB3 : //Rafting
            return "rafting".i18n
        case 0xB4 : //Skateboarding
            return "skateboarding"
        case 0xB5 : //Rock climbing
            return "rock climbing"
        case 0xB6 : //Bungee jumping
            return "bungee jumping".i18n
        case 0xB7 : //Parkour
            return "parkour".i18n
        case 0xB8 : //BMX
            return "BMX"
        case 0xBB : //Soccer volleyball
            return "Soccer volleyball"
            
        case 0xBC : //Standing water skiing
            return "Standing water skiing".i18n
        case 0xBD : //Cross-country running
            return "Cross country running".i18n
        case 0xBE : //Crunches
            return "Roll up the belly".i18n
        case 0xBF : //Burpees
            return "Wave ratio hop".i18n
        case 0xC0 : //Kabaddi
            return "".i18n
        case 0xC1 : //Outdoor play (KR01)
            return "Kabaddi".i18n
        case 0xC2 : //Other sports (KR01)
            return "Other Sports (KR01)".i18n
        case 0xC3 : //Trampoline
            return "trampoline".i18n
       
//        case 0xC4 : //Hula hoop
//        case 0xC5 : //Auto racing
//        case 0xC6 : //Battle ropes
//        case 0xC7 : //Skydiving
//        case 0xC8 : //Orienteering
//        case 0xC9 : //Mountain biking
//        case 0xCA : //Beach tennis
//        case 0xCB : //Smart rope skipping
//        case 0xCC : //Pickleball
//        case 0xCD : //Wheelchair sports
//        case 0xCE : //Physical training
//        case 0xCF : //Kettlebell training
//        case 0xD0 : //Group gymnastics
//        case 0xD1 : //Cross fit
//        case 0xD2 : //Obstacle race
//        case 0xD3 : //Scooter
//        case 0xD4 : //Glider
//        case 0xD5 : //Skiing
//        case 0xD6 : //Snowboard skiing
//        case 0xD7 : //Combat aerobics
//        case 0xD8 : //Kendo
//        case 0xD9 : //Tai chi chuan
//        case 0xDA : //Mixed martial arts
//        case 0xDB : //Wrestling
//        case 0xDC : //Belly dance
//        case 0xDD : //Jazz dance
//        case 0xDE : //Latin dance
//        case 0xDF : //Tap dance
//        case 0xE0 : //Other dance
//        case 0xE1 : //Beach volleyball
//        case 0xE2 : //Gateball
//        case 0xE3 : //Polo
//        case 0xE4 : //Lacrosse
//        case 0xE5 : //Canoeing
//        case 0xE6 : //Stand-up paddleboarding
//        case 0xE7 : //Battle games
//        case 0xE8 : //Tug of war
//        case 0xE9 : //Swing
//        case 0xEA : //Equestrian
        case 0xEB : //Track and field
            return "Track and field".i18n
//        case 0xEC : //Stair climber
//        case 0xED : //Flexibility training
//        case 0xEE : //Chess
//        case 0xEF : //Checkers
//        case 0xF0 : //Go
//        case 0xF1 : //Bridge
//        case 0xF2 : //Board games
//        case 0xF3 : //Folk dance
//        case 0xF4 : //Hip-hop dance
//        case 0xF5 : //Pole dance
//        case 0xF6 : //Breakdance
//        case 0xF7 : //Modern dance
//        case 0xF8 : //Muay Thai
//        case 0xF9 : //Judo
//        case 0xFA : //Jiu-jitsu
//        case 0xFB : //Jai alai
//        case 0xFC : //Snowmobile
//        case 0xFD : //Paragliding
//        case 0xFE : //Lacrosse (long stick)
//        case 0xFF : //American football
        default:
            return "\(sportType)"
        }
    }
}

extension SportListVC {
    
    override func numberOfSections(in tableView: UITableView) -> Int {
        return 1
    }
    
    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        
        return self.screenInfoReplyModel?.sportItems?.count ?? 0;
    }
    
    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: sportCellIdentifier, for: indexPath)
        if let count = self.screenInfoReplyModel?.sportItems?.count,
           let sportItems = self.screenInfoReplyModel?.sportItems, count > 0 {
            cell.textLabel?.text = self.getSportName(sportItems[indexPath.row].sportType)
        }
        return cell
    }
    
    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        if let count = self.screenInfoReplyModel?.sportItems?.count,
           let sportItems = self.screenInfoReplyModel?.sportItems, count > 0 {
            let sportScrrenEditVC = SportScreenEditVC()
            sportScrrenEditVC.screenInfoReplyModel = self.screenInfoReplyModel
            sportScrrenEditVC.sportType = sportItems[indexPath.row].sportType
            self.navigationController?.pushViewController(sportScrrenEditVC, animated: true)
        }
       
    }
}

