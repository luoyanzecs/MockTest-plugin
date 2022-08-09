package com.ctrip.hotel.wireless.hotelpositioninfoservice.repository.impl;

import com.ctrip.gs.food.internal.service.soa.RestaurantDto;
import com.ctrip.gs.food.internal.service.soa.SearchRestaurantRequestType;
import com.ctrip.gs.food.internal.service.soa.SearchRestaurantResponseType;
import com.ctrip.hotel.wireless.cache.HotelSimpleCache;
import com.ctrip.hotel.wireless.config.MapConfig;
import com.ctrip.hotel.wireless.config.spring.Config;
import com.ctrip.hotel.wireless.entity.HotelSimpleInfo;
import com.ctrip.hotel.wireless.hotelpositioninfoservice.common.CoordHelper;
import com.ctrip.hotel.wireless.hotelpositioninfoservice.model.poi.NearbyPoi;
import com.ctrip.hotel.wireless.hotelpositioninfoservice.model.poi.NearbyPoiRequest;
import com.ctrip.hotel.wireless.hotelpositioninfoservice.model.poi.RestaurantModel;
import com.ctrip.hotel.wireless.hotelpositioninfoservice.repository.NearByPoiRepository;
import com.ctrip.hotel.wireless.hotelpositioninfoservice.repository.SuggestPoiListRepository;
import com.ctrip.hotel.wireless.hotelpositioninfoservice.repository.wrapper.NearbyFacilityWrapper;
import com.ctrip.hotel.wireless.soa.SOAServiceAgent;
import com.ctrip.hotelwireless.common.util.MiscUtils;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.tempuri.foodfrontendcontract_v1.GetAroundRestaurantsRequestType;
import org.tempuri.foodfrontendcontract_v1.GetAroundRestaurantsResponseType;
import org.tempuri.foodfrontendcontract_v1.GetRestaurantsByPoiIdsRequestType;
import org.tempuri.foodfrontendcontract_v1.GetRestaurantsByPoiIdsResponseType;
import org.tempuri.foodfrontendtypes_v1.RestaurantBriefInfo;
import org.tempuri.foodfrontendtypes_v1.RestaurantInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 周边餐饮信息实现
 *
 * Created by jieming.chen on 2020/1/2
 */
@Component(value = "nearbyRestaurantRepository")
@Config
@PrepareForTest({
        NearbyRestaurantRepositoryImpl.class,
        SuggestPoiListRepository.class,
        SOAServiceAgent.class,
        NearbyFacilityWrapper.class,
        MapConfig.class,
        Logger.class,
        MiscUtils.class,
        LoggerFactory.class,
        CoordHelper.class,
})
@XXX({"1", "2"})
public class NearbyRestaurantRepositoryImpl implements NearByPoiRepository {

    @Autowired
    private SOAServiceAgent soaServiceAgent;

    private final Logger logger= LoggerFactory.getLogger(NearbyRestaurantRepositoryImpl.class, "123", soaServiceAgent);

    @Autowired
    private NearbyFacilityWrapper nearbyFacilityWrapper;
    @Autowired
    private SuggestPoiListRepository suggestPoiListRepository;
    @Config
    private MapConfig mapConfig;

    private String abc = HotelSimpleCache.get(123);

    public NearbyRestaurantRepositoryImpl() {}

    public NearbyRestaurantRepositoryImpl(String s) {
        System.out.println("-----");
    }

    @Override
    public List<NearbyPoi> getNearbyPoi(NearbyPoiRequest nearbyPoiRequest) {

        SearchRestaurantRequestType searchRestaurantRequestType = getSearchRestaurantRequestType(nearbyPoiRequest);
        String a = "123";

        if (mapConfig.getBoolean("searchRestaurantSOA", false)
                && null != searchRestaurantRequestType
                && nearbyPoiRequest.getSourceFrom() == 0) {
            try {
                SearchRestaurantResponseType searchRestaurantResponseType = soaServiceAgent
                        .invokeSOA("searchRestaurantV2",
                                searchRestaurantRequestType,
                                SearchRestaurantResponseType.class);
                if (searchRestaurantResponseType != null
                        && MiscUtils.isNotEmpty(searchRestaurantResponseType.getRestaurants())) {
                    return convertsTo(searchRestaurantResponseType.getRestaurants(), nearbyPoiRequest);
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

        GetAroundRestaurantsRequestType request = suggestPoiListRepository.getAroundRestaurantsRequestNew(nearbyPoiRequest);
        try {
            GetAroundRestaurantsResponseType response = soaServiceAgent.invokeSOA("getAroundRestaurantsNew", request, GetAroundRestaurantsResponseType.class);
            if (response == null || CollectionUtils.isEmpty(response.getRestaurants())) {
                return Lists.newArrayList();
            }
            return convertTo(response.getRestaurants(), response.getHybirdUrl(), nearbyPoiRequest);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return Lists.newArrayList();
    }

    private List<NearbyPoi> convertsTo(List<RestaurantDto> restaurantDtos, NearbyPoiRequest nearbyPoiRequest) {

        return this.nearbyFacilityWrapper.wraps(restaurantDtos, nearbyPoiRequest);
    }

    private SearchRestaurantRequestType getSearchRestaurantRequestType(NearbyPoiRequest nearbyPoiRequest) {
        // 判空，判经纬度是否合法
        if (nearbyPoiRequest == null ||
                !CoordHelper.checkIsValid(nearbyPoiRequest.getLatitude(), nearbyPoiRequest.getLongitude())) {
            return null;
        }

        SearchRestaurantRequestType requestType = new SearchRestaurantRequestType();
        requestType.setLat(nearbyPoiRequest.getLatitude());
        requestType.setLon(nearbyPoiRequest.getLongitude());
        requestType.setLimit(20);
        int pageIndex = nearbyPoiRequest.getPageIndex();
        if (pageIndex <= 0) {
            requestType.setOffset(0);
        } else {
            requestType.setOffset((pageIndex - 1) * 20);
        }
        requestType.setDistance(10.0);
        requestType.setSourceType("hotel_map_around");
        requestType.setCoordType(1);
        if (nearbyPoiRequest.getOrderBy() != null && "distance_asc".equals(nearbyPoiRequest.getOrderBy())) {
            requestType.setOrderType(23);
        } else {
            requestType.setOrderType(1);
        }
        return requestType;
    }

    private List<NearbyPoi> convertTo(List<RestaurantInfo> restaurantInfos, String hybirdUrl, NearbyPoiRequest nearbyPoiRequest) {
        // 过滤有价格的
        List<RestaurantModel> restaurantModelList = setRestaurantsByPoiIdsNew(restaurantInfos);
        return this.nearbyFacilityWrapper.wrap(restaurantInfos, restaurantModelList, hybirdUrl, nearbyPoiRequest);
    }

    private List<RestaurantModel> setRestaurantsByPoiIdsNew(List<RestaurantInfo> restaurantInfos) {
        GetRestaurantsByPoiIdsRequestType soaRequest = new GetRestaurantsByPoiIdsRequestType();
        soaRequest.setPoiIds(restaurantInfos.stream()
                .map(RestaurantInfo::getPoiId).collect(Collectors.toList()));
        try {
            GetRestaurantsByPoiIdsResponseType soaResponse = soaServiceAgent.invokeSOA("getRestaurantsByPoiIdsNew", soaRequest, GetRestaurantsByPoiIdsResponseType.class);
            if (soaResponse == null || CollectionUtils.isEmpty(soaResponse.getRestaurants())) {
                return Lists.newArrayList();
            }
            return getSetRestaurantPriceNew(soaResponse.getRestaurants(), restaurantInfos);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return Lists.newArrayList();
    }

    private List<RestaurantModel> getSetRestaurantPriceNew(List<RestaurantBriefInfo> results, List<RestaurantInfo> restaurants) {
        List<RestaurantModel> restaurantModelList = new ArrayList<>();
        for (RestaurantInfo restaurant : restaurants) {
            final long poiId = restaurant.getPoiId();
            RestaurantBriefInfo suggestPoiDto = results.stream()
                    .filter(item -> poiId == item.getPoiId()).findFirst().orElse( null);
            // 设置价格和poid
            if (suggestPoiDto != null) {
                RestaurantModel restaurantModel = new RestaurantModel();
                restaurantModel.setAveragePrice(restaurant.getAveragePrice());
                restaurantModel.setPoiId(restaurant.getPoiId());
                restaurantModelList.add(restaurantModel);
            }
        }
        return restaurantModelList;
    }
}



