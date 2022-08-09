package com.ctrip.hotel.wireless.hotelpositioninfoservice.repository.impl;


import com.ctrip.hotel.wireless.hotelpositioninfoservice.repository.SuggestPoiListRepository;
import com.ctrip.hotel.wireless.soa.SOAServiceAgent;
import com.ctrip.hotel.wireless.hotelpositioninfoservice.repository.wrapper.NearbyFacilityWrapper;
import com.ctrip.hotel.wireless.config.MapConfig;
import org.slf4j.Logger;
import com.ctrip.hotelwireless.common.util.MiscUtils;
import org.slf4j.LoggerFactory;
import com.ctrip.hotel.wireless.hotelpositioninfoservice.common.CoordHelper;
import com.ctrip.hotel.wireless.hotelpositioninfoservice.repository.impl.NearbyRestaurantRepositoryImpl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.MockPolicy;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * generated by MockGen;
 * @Author MockGen
 * @Date 2022年8月6日 下午12:14:05
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest({
        NearbyRestaurantRepositoryImpl.class,
        SuggestPoiListRepository.class,
        SOAServiceAgent.class,
        NearbyFacilityWrapper.class,
        My.class
})
@SuppressStaticInitializationFor({
        "com.ctrip.hotel.wireless.hotelpositioninfoservice.repository.SuggestPoiListRepository",
        "com.ctrip.hotel.wireless.soa.SOAServiceAgent",
        "com.ctrip.hotel.wireless.hotelpositioninfoservice.repository.wrapper.NearbyFacilityWrapper",
        "com.ctrip.hotel.wireless.config.MapConfig",
        "org.slf4j.Logger",
})
public class NearbyRestaurantRepositoryImplTest {

    @InjectMocks
    private NearbyRestaurantRepositoryImpl testObj;

    @Mock
    private SuggestPoiListRepository suggestPoiListRepository;
    @Mock
    private SOAServiceAgent sOAServiceAgent;
    private NearbyFacilityWrapper nearbyFacilityWrapper;

    private String a = "123";

    public void haha() {
        PowerMockito.mockStatic(LoggerFactory.class);

        testObj = new NearbyRestaurantRepositoryImpl();
    }

    @BeforeClass
    public static void beforeClass() {
        System.out.println("*******");
    }

    @Before
    public void setUp1() {
        System.out.println("++++++");
    }
    @Before
    public void setUp() {
        PowerMockito.mockStatic(LoggerFactory.class);
        PowerMockito.when(LoggerFactory.getLogger(Mockito.any())).thenReturn(1);
        PowerMockito.mockStatic(CoordHelper.class);
        PowerMockito.when(sOAServiceAgent.call("1")).thenReturn("1");
        System.out.println("-----");

        testObj = new NearbyRestaurantRepositoryImpl();
    }

}
