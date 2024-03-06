package com.blk.platform_castle;

import android.os.SystemClock;
import android.util.Log;

import com.blk.platform.ISystem;

import CTOS.CtSystem;
import CTOS.CtSystemException;

class SystemCtos extends ISystem {
    CtSystem system = PlatfromCtos._system;

    public enum EModuleId {
        ID_BOOTSULD(0),
        CRYPTO_HAL(1),
        ID_LINUX_KERNEL(3),
        ID_SECURITY_KO(4),
        ID_SYSUPD_KO(5),
        ID_KMS(6),
        ID_CADRV_KO(7),
        ID_CAUSB_KO(8),
        ID_LIBCAUART_SO(9),
        ID_LIBCAUSBH_SO(10),
        ID_LIBCAMODEM_SO(11),
        ID_LIBCAETHERNET_SO(12),
        ID_LIBCAFONT_SO(13),
        ID_LIBCALCD_SO(14),
        ID_LIBCAPRT_SO(15),
        ID_LIBCARTC_SO(16),
        ID_LIBCAULDPM_SO(17),
        ID_LIBCAPMODEM_SO(18),
        ID_LIBCAGSM_SO(19),
        ID_LIBCAEMVL2_SO(20),
        ID_LIBCAKMS_SO(21),
        ID_LIBCAFS_SO(22),
        ID_LIBCABARCODE_SO(23),
        ID_CRADLE_MP(24),
        ID_LIBTLS_SO(25),
        ID_LIBCLVW_SO(26),
        ID_LIBCTOSAPI_SO(27),
        ID_SAM_KO(28),
        ID_CLVWM_MP(29),
        ID_ROOTFS(30),
        ID_BIOS(31),
        ID_CIF_KO(32),
        ID_CLDRV_KO(33),
        ID_TMS(34),
        ID_ULDPM(35),
        ID_SC_KO(36),
        ID_EMV(37),
        ID_EMVCL(38);
        private final int id;

        EModuleId(int id) {
            this.id = id;
        }

        public int getValue() {
            return id;
        }
    }

    public void PrintVersions()
    {
        Log.i("blktechpos", "ISystem Module versions ----------");
        for (EModuleId day : EModuleId.values()) {
            try {
                Log.i("blktechpos", day.name() + " : " + system.getModuleVersion(day.id));
        } catch (CtSystemException e) {
                //e.printStackTrace();
            }
        }
        Log.i("blktechpos", "---------------------------------");
    }

    @Override
    public void beepOk() {
        system.beep();// sound(2700, 20);
    }

    @Override
    public void beepErr() {
        system.beep();//system.sound(750, 20);
        SystemClock.sleep(200);
        system.beep();//system.sound(750, 20);
    }

    @Override
    public String Serial() throws CtSystemException {
        //return new String(system.getFactorySN());
        return "0005021717001600";
    }

    @Override
    public String Model() throws CtSystemException {
        //return (system.getDeviceModel() == 12) ? "S1000" : "UNKONWN";
        return "V1000";
    }

    @Override
    public void setSystemTime(int year, int month, int day, int hour, int minute, int second) {
        system.setSystemTime(year, month, day, hour, minute, second);
    }
}
